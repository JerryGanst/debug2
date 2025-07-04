package org.example.ai_api.Service;

import io.minio.*;
import org.example.ai_api.Bean.Entity.FileConversionTask;
import org.example.ai_api.Bean.Entity.FileInfo;
import org.example.ai_api.Bean.Entity.KnowledgeFileInfo;
import org.example.ai_api.Bean.Entity.Target;
import org.example.ai_api.Bean.Model.FileInfoFormSystem;
import org.example.ai_api.Exception.DataNotComplianceException;
import org.example.ai_api.Exception.NotAccessedException;
import org.example.ai_api.Exception.NotFoundException;
import org.example.ai_api.Repository.FileConversionTaskRepository;
import org.example.ai_api.Repository.FilesRepository;
import org.example.ai_api.Repository.KnowledgeFileRepository;
import org.example.ai_api.Repository.TargetRepository;
import org.example.ai_api.Strategy.KnowledgeFileSort.KnowledgeFileSortContext;
import org.example.ai_api.Strategy.KnowledgeFileSort.KnowledgeFileSortStrategy;
import org.example.ai_api.Utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识库文件相关服务.
 */
@Service
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    @Autowired
    private SystemFileService systemFileService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private FilesRepository filesRepository;
    @Autowired
    private FileConverter fileConverter;
    @Autowired
    private KnowledgeFileRepository knowledgeFileRepository;
    @Autowired
    private FileConversionTaskRepository fileConversionTaskRepository;
    @Autowired
    private TargetRepository targetRepository;
    @Autowired
    private KnowledgeFileSortContext knowledgeFileSortContext;
    @Autowired
    private UserPermissionService userPermissionService;
    @Autowired
    private FileContentReader fileContentReader;
    @Autowired
    private MinioOperations minioOperations;
    @Autowired
    @Qualifier("StreamWebClient")
    private WebClient webClient;
    @Value("${minio.bucketName}")
    private String bucketName;
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${libreoffice_tasks}")
    private String libreOfficeTasks;

    /**
     * 根据文件名获得文件链接.
     *
     * @param fileName 文件名
     * @return 文件信息结构体
     */
    public FileInfo findByFileName(String fileName) {
        return filesRepository.findByFileName(fileName);
    }

    public List<KnowledgeFileInfo> saveAll(List<KnowledgeFileInfo> knowledgeFileInfos) {
        return knowledgeFileRepository.saveAll(knowledgeFileInfos);
    }

    public KnowledgeFileInfo getFileById(String fileId) {
        return knowledgeFileRepository.findById(fileId).orElseThrow(() -> new NotFoundException("文件不存在"));
    }

    public ResponseEntity<Resource> getKnowledgeFileById(String fileId) throws Exception {
        KnowledgeFileInfo fileInfo = knowledgeFileRepository.findById(fileId).orElseThrow(() -> new NotFoundException("文件不存在"));
        logger.info("获取文件{}", fileInfo);
        String path = fileInfo.getStoragePath().replace(bucketName + "/", "");
        InputStream fileStream = minioOperations.getFileStream(path);
        return Utils.exchangeInputStreamToResource(fileStream, fileInfo.getFileName());
    }

    /**
     * 根据需求排序文件列表.
     *
     * @param fileList 待排序文件列表
     * @param sortType 排序方式
     * @return 排序处理后的列表
     */
    public List<KnowledgeFileInfo> sortFileList(List<KnowledgeFileInfo> fileList, String sortType, boolean increase) {
        KnowledgeFileSortStrategy strategy = knowledgeFileSortContext.getStrategy(sortType);
        List<KnowledgeFileInfo> result;
        if (strategy == null) {
            result = fileList;
        } else {
            result = strategy.sort(fileList);
            if (!increase) {
                Collections.reverse(result);
            }
        }
        return result;
    }

    /**
     * 搜索文件信息
     *
     * @param keyword  用户输入的关键字(支持模糊查询)
     * @param target   查询的领域
     * @param userId   查询者id
     * @param isPublic 是否查询公共领域
     * @return 查询结果
     */
    public List<KnowledgeFileInfo> searchFile(String keyword, String target, String userId, boolean isPublic) {
        Criteria criteria = new Criteria();
        criteria.and("isPublic").is(isPublic);
        if (isPublic) {
            //前置权限检查
            if (!userPermissionService.checkUserPermission(userId, target).isRead()) {
                throw new NotAccessedException("无权限访问");
            }
            criteria.and("fileTarget").is(target);
        } else {
            criteria.and("uploaderId").is(userId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            criteria.and("fileName").regex(keyword);
        }
        return mongoTemplate.find(Query.query(criteria), KnowledgeFileInfo.class);
    }

    /**
     * 知识库公共文件上传.
     *
     * @param files  文件列表
     * @param userId 用户id
     * @return 上传后的文件信息列表
     */
    public List<KnowledgeFileInfo> knowledgeFileUpload(List<MultipartFile> files, String userId, String target, boolean isPublic) throws Exception {
        //前置检查,返回转换后的在服务器合法的文件名列表
        List<String> fileNames = checkBeforeUpload(files, userId);
        //检查文件是否重复，并获取hash值
        List<String> fileHash = checkFileHash(files, isPublic, userId, target);
        //检查是否存在文件重名
        checkFileExist(fileNames, target, userId, isPublic);
        //文件上传,返回文件路径列表
        List<Path> filePath = uploadFiles(files, fileNames, target, userId, isPublic);
        logger.info("用户{}上传{}个文件到领域{}", userId, files.size(), target);
        //构建并返回上传文件信息
        return buildUploadFileInfo(files, fileHash, userId, filePath, fileNames, target, isPublic);
    }

    /**
     * 公共知识库文件删除
     *
     * @param fileId 将删除的文件id
     * @param userId 操作者id
     * @throws Exception 操作过程报错
     */
    public void knowledgeFileDelete(String fileId, String userId) throws Exception {
        logger.info("用户{}尝试删除文件{}", userId, fileId);
        KnowledgeFileInfo fileInfo = knowledgeFileRepository.findById(fileId).orElseThrow(() -> new NotFoundException("文件不存在"));
        String name = minioOperations.createKnowledgeFileName(fileInfo.getFileName(), fileInfo.getFileTarget(), fileInfo.getUploaderId(), fileInfo.isPublic());
        minioOperations.deleteFile(name);
        deleteConvertKnowledgeFile(fileId);
        knowledgeFileRepository.deleteById(fileId);
    }

    /**
     * 根据id获得文件文本
     */
    public String getContentById(String id) throws Exception {
        logger.info("根据id{}获得文件文本", id);
        KnowledgeFileInfo fileInfo = knowledgeFileRepository.findById(id).orElseThrow(() -> new NotFoundException("文件不存在"));
        InputStream stream = minioOperations.getFileStream(fileInfo.getStoragePath().replace(bucketName + "/", ""));
        return fileContentReader.readFile(stream, fileInfo.getFileName());
    }

    /**
     * 删除源文件时，同步删除转换任务或转换后的文件
     *
     * @param fileId 原文件id
     * @throws Exception 操作过程报错
     */
    private void convertFilesDelete(String fileId) throws Exception {
        logger.info("开始删除文件{}转换后的文件", fileId);
        FileConversionTask task = fileConversionTaskRepository.findByFileId(fileId);
        if (task != null) {
            fileConversionTaskRepository.deleteById(task.getTaskId());
        } else {
            return;
        }
        if ("COMPLETED".equals(task.getStatus())) {
            minioOperations.deleteFile(task.getConvertedFilePath());
        }
        logger.info("文件{}转换文件已删除", fileId);
    }

    public void deleteConvertKnowledgeFile(String fileId) throws Exception {
        logger.info("删除文件{}转换后的文件", fileId);
        KnowledgeFileInfo info = knowledgeFileRepository.findById(fileId).orElseThrow(() -> new NotFoundException("文件不存在"));
        if(info.getConvertPath()!=null&&!info.getConvertPath().isEmpty()){
            minioOperations.deleteFile(info.getConvertPath());
        }
        logger.info("文件{}转换已删除", fileId);
    }

    /**
     * 公共知识库文件下载(3分钟链接过期)
     *
     * @param fileId 将下载的文件id
     * @return 文件下载链接
     * @throws Exception 操作过程报错
     */
    public String getDownloadUrl(String fileId) throws Exception {
        KnowledgeFileInfo file = knowledgeFileRepository.findById(fileId).orElseThrow(() -> new NotFoundException("文件不存在"));
        String name = file.getStoragePath().replace("/" + bucketName + "/", "");
        // 添加强制下载的响应头
        Map<String, String> reqParams = new HashMap<>();
        reqParams.put("response-content-disposition",
                "attachment; filename=\"" + URLEncoder.encode(file.getFileName(), "UTF-8") + "\"");
        return minioOperations.getDownloadUrl(name, 180 , reqParams);
    }

    /**
     * 检查是否有对某个文件的访问权限
     *
     * @param fileId 文件id
     * @param userId 操作者id
     */
    public void checkUserPermissionForFile(String fileId, String userId) {
        KnowledgeFileInfo fileInfo = getFileById(fileId);
        //权限检查
        if (fileInfo.isPublic()) {
            //属于公共领域权限检查
            if (!userPermissionService.checkUserPermission(userId, fileInfo.getFileTarget()).isRead()) {
                throw new NotAccessedException("无权限访问该领域");
            }
        } else {
            //属于私有领域权限检查
            if (!userId.equals(fileInfo.getUploaderId())) {
                throw new NotAccessedException("无权限访问该文件");
            }
        }
    }

    /**
     * 上传文件，同步创建转换任务
     *
     * @param files 完成上传后的文件列表
     */
    public void buildFileConversionTask(List<KnowledgeFileInfo> files) {
        List<FileConversionTask> result = new ArrayList<>();
        for (KnowledgeFileInfo file : files) {
            FileConversionTask task = new FileConversionTask();
            task.setFileId(file.getId());
            task.setCreateTime(LocalDateTime.now());
            task.setTargetFormat(getTargetFormat(file.getOriginalFileName()));
            task.setStatus("PENDING");
            result.add(task);
        }
        fileConversionTaskRepository.saveAll(result);
    }

    @Cacheable(value = "filesByTarget", key = "#target", unless = "#result == null")
    public List<FileInfoFormSystem> getFileByTarget(String target) throws Exception {
        List<FileInfoFormSystem> fileInfos = systemFileService.getFileInfoFromSystem();
        Target targetInfo = targetRepository.findByTargetName(target);
        if (targetInfo == null) {
            throw new NotFoundException("领域不存在");
        }
        String finalCategory = targetInfo.getCategory();
        //找到对应类别文件的父文件夹id
        String id = fileInfos.stream()
                .filter(file -> file.getCategory().equals(finalCategory))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("文件目录不存在"))
                .getId();
        //根据父文件夹id在列表中筛选数据
        return fileInfos.stream()
                .filter(file -> file.getFatherId().equals(id))
                .collect(Collectors.toList());
    }

    public List<KnowledgeFileInfo> changeToKnowledgeFile(List<FileInfoFormSystem> files) {
        List<KnowledgeFileInfo> result = new ArrayList<>();
        for (FileInfoFormSystem file : files) {
            KnowledgeFileInfo fileInfo = new KnowledgeFileInfo();
            fileInfo.setFileName(file.getCategory());
            fileInfo.setFileType(Utils.getFileExtension(file.getCategory()));
            fileInfo.setPublic(true);
            fileInfo.setId(file.getFileKey());
            fileInfo.setCreateTime(file.getCreateTime());
            result.add(fileInfo);
        }
        return result;
    }

    @Async
    public void covertPrivateKnowledgeFiles(List<KnowledgeFileInfo> tasks){
        webClient.post()
                .uri(libreOfficeTasks)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tasks)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private String getTargetFormat(String fileName) {
        if (fileName.endsWith("ppt") || fileName.endsWith("pptx")) {
            return "pptx";
        } else {
            return "pdf";
        }
    }

    /**
     * 前置检查,返回转换后的在服务器合法的文件名列表
     *
     * @param files  需要上传的文件列表
     * @param userId 上传用户的id
     * @return 上传文件在服务器合法的文件名列表
     */
    private List<String> checkBeforeUpload(List<MultipartFile> files, String userId) {
        logger.info("用户{}上传文件,进行前置检查", userId);
        //文件非空检查
        if (files == null || files.isEmpty()) {
            throw new DataNotComplianceException("文件列表为空");
        }
        files.forEach(file -> {
            if (file.isEmpty()) {
                throw new DataNotComplianceException("文件列表中存在空文件");
            }
        });
        //用户id非空检查
        if (userId == null || userId.isEmpty()) {
            throw new DataNotComplianceException("用户id为空");
        }
        //文件名非空检查
        files.forEach(file -> {
            if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
                throw new DataNotComplianceException("文件列表中存在文件名为空的文件");
            }
        });
        //文件名转换，包含文件名转换和文件扩展名转换
        return files.stream()
                .map(file -> Utils.renameFileToUbuntu(Objects.requireNonNull(file.getOriginalFilename())))
                .collect(Collectors.toList());
    }

    /**
     * 上传前的文件格式转换 (word,txt -> pdf; ppt -> pptx)
     *
     * @param files 需要转换的文件列表
     * @return 转换后的文件列表
     * @throws Exception 转换过程中的异常
     */
    private List<byte[]> convertBeforeUpload(List<MultipartFile> files) throws Exception {
        List<byte[]> result = new ArrayList<>();
        for (MultipartFile file : files) {
            result.add(convert(file));
        }
        return result;
    }

    /**
     * 调用文件转换的工具类，返回转换后的二进制文件
     *
     * @param file 需要转换的文件
     * @return 转换后的二进制文件
     * @throws Exception 转换过程中的异常
     */
    private byte[] convert(MultipartFile file) throws Exception {
        String extension = Utils.getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
        logger.info("文件{}进行格式转换", file.getOriginalFilename());
        if ("ppt".equals(extension)) {
            return fileConverter.convert(file, "pptx");
        } else {
            return fileConverter.convert(file, "pdf");
        }
    }

    /**
     * 文件上传,返回文件路径列表
     *
     * @param files     需要上传的文件列表
     * @param fileNames 上传文件在服务器合法的文件名列表
     * @param target    上传文件所属领域
     * @return 上传文件在服务器的路径列表
     */
    private List<Path> uploadFiles(List<MultipartFile> files, List<String> fileNames, String target, String userId, boolean isPublic) {
        List<Path> result = new ArrayList<>();
        List<String> fileUploads = new ArrayList<>();
        for (int index = 0; index < files.size(); index++) {
            String name = minioOperations.createKnowledgeFileName(fileNames.get(index), target, userId, isPublic);
            String contentType = ContentTypeDetector.getContentType(fileNames.get(index));
            try (InputStream inputStream = files.get(index).getInputStream()) {
                minioOperations.uploadFile(name, inputStream, files.get(index).getSize() ,contentType);
                fileUploads.add(name);
                URI uri = URI.create(String.format("%s/%s/%s", endpoint, bucketName, name));
                result.add(Paths.get(uri.getPath()));
            } catch (Exception e) {
                // 发生异常时删除已上传的所有文件
                deleteUploadedFiles(fileUploads);
                logger.info("文件上传过程报错，错误前上传文件已回滚");
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * 上传报错时，清除已上传文件
     *
     * @param fileUploads 已上传文件名
     */
    private void deleteUploadedFiles(List<String> fileUploads) {
        logger.info("文件上传过程出错，开始回滚所有上传文件");
        for (String fileName : fileUploads) {
            try {
                minioOperations.deleteFile(fileName);
            } catch (Exception e) {
                // 处理删除异常（例如记录日志）
                logger.info("回滚删除文件{}失败: {}", fileName, e.getMessage());
            }
        }
    }

    /**
     * 检查文件是否会重名
     *
     * @param fileNames 上传的文件名
     * @param target    目标领域
     */
    private void checkFileExist(List<String> fileNames, String target, String userId, boolean isPublic) {
        logger.info("检查文件是否已经存在");
        if (Utils.checkDuplicateStringInList(fileNames)) {
            throw new DataNotComplianceException("文件名重复");
        }
        List<KnowledgeFileInfo> existFiles;
        if (isPublic) {
            existFiles = knowledgeFileRepository.findByFileTargetAndIsPublicAndFileNameIn(target, true, fileNames);
        } else {
            existFiles = knowledgeFileRepository.findByUploaderIdAndIsPublicAndFileNameIn(userId, false, fileNames);
        }
        if (existFiles != null && !existFiles.isEmpty()) {
            throw new DataNotComplianceException("文件名重复");
        }
    }

    /**
     * 根据内容哈希判定文件是否重复
     *
     * @param files    待上传文件数组
     * @param isPublic 是否公有
     * @param userId   上传者id
     * @param target   所属领域
     * @throws IOException 存在重复时以错误形式抛出
     */
    private List<String> checkFileHash(List<MultipartFile> files, boolean isPublic, String userId, String target) throws IOException {
        List<String> fileHash = new ArrayList<>();
        for (MultipartFile file : files) {
            fileHash.add(Utils.getHash(file));
        }
        if (Utils.checkDuplicateStringInList(fileHash)) {
            throw new DataNotComplianceException("上传文件中有相同内容文件存在");
        }
        List<KnowledgeFileInfo> existHash;
        if (isPublic) {
            existHash = knowledgeFileRepository.findByFileTargetAndIsPublicAndHashCodeIn(target, true, fileHash);
        } else {
            existHash = knowledgeFileRepository.findByUploaderIdAndIsPublicAndHashCodeIn(userId, false, fileHash);
        }
        if (existHash != null && !existHash.isEmpty()) {
            throw new DataNotComplianceException("上传文件与已上传文件存在重复");
        }
        return fileHash;
    }

    /**
     * 构建并返回上传文件信息
     *
     * @param files     需要上传的文件列表
     * @param userId    上传用户的id
     * @param filePath  上传文件在服务器的路径列表
     * @param fileNames 上传文件在服务器合法的文件名列表
     * @param target    上传文件所属领域
     * @return 上传文件信息列表
     */
    private List<KnowledgeFileInfo> buildUploadFileInfo(List<MultipartFile> files, List<String> fileHash, String userId, List<Path> filePath, List<String> fileNames, String target, boolean isPublic) {
        logger.info("用户{}上传{}个文件,构建并返回上传文件信息", userId, files.size());
        List<KnowledgeFileInfo> result = new ArrayList<>();
        for (int index = 0; index < files.size(); index++) {
            KnowledgeFileInfo fileInfo = new KnowledgeFileInfo();
            fileInfo.setOriginalFileName(files.get(index).getOriginalFilename());
            fileInfo.setFileName(fileNames.get(index));
            fileInfo.setUploaderId(userId);
            fileInfo.setCreateTime(LocalDateTime.now());
            fileInfo.setUpdateTime(LocalDateTime.now());
            fileInfo.setFileTarget(isPublic ? target : "");
            fileInfo.setPublic(isPublic);
            fileInfo.setHashCode(fileHash.get(index));
            fileInfo.setFileType(
                    Utils.getFileExtension(
                            Objects.requireNonNull(
                                    files.get(index)
                                            .getOriginalFilename()
                            )
                    )
            );
            fileInfo.setStoragePath(
                    filePath.get(index)
                            .toString()
                            .replace("\\", "/")
            );
            fileInfo.setFileSize(
                    getFileSizeOnServer(
                            filePath.get(index)
                                    .toString()
                                    .replace("\\", "/")
                                    .replace(bucketName + "/", "")
                    )
            );
            result.add(fileInfo);
        }
        return result;
    }

    private long getFileSizeOnServer(String path) {
        try {
            StatObjectResponse stat = minioOperations.getObjectStat(path);
            return stat.size();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
