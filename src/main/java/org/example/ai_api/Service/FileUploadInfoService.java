package org.example.ai_api.Service;

import org.example.ai_api.Bean.Entity.FileUpload;
import org.example.ai_api.Exception.DataNotComplianceException;
import org.example.ai_api.Exception.NotFoundException;
import org.example.ai_api.Repository.FileUploadInfoRepository;
import org.example.ai_api.Utils.FileContentReader;
import org.example.ai_api.Utils.MinioOperations;
import org.example.ai_api.Utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户问答过程文件上传相关服务.
 */
@Service
public class FileUploadInfoService {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadInfoService.class);
    /**
     * The File upload info repository.
     */
    @Autowired
    private FileUploadInfoRepository fileUploadInfoRepository;
    /**
     * The Mongo template.
     */
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private FileContentReader fileContentReader;
    @Autowired
    private MinioOperations minioOperations;
    @Autowired
    private FileService fileService;

    /**
     * 文件批量上传.
     *
     * @param fileUploads 批量上传的文件
     * @return 上传后的文件信息列表
     */
    public List<FileUpload> saveAll(List<FileUpload> fileUploads) {
        return fileUploadInfoRepository.saveAll(fileUploads);
    }

    /**
     * 根据id获得文件文本.
     *
     * @param id 文件id
     * @return 对应id文件内的文本
     */
    public String getContentById(String id) throws Exception {
        logger.info("根据id{}获得文件文本", id);
        FileUpload fileUpload = fileUploadInfoRepository.findById(id).orElseThrow(() -> new NotFoundException("文件不存在"));
        InputStream stream = minioOperations.getFileStream(fileUpload.getFilePath());
        return fileContentReader.readFile(stream, fileUpload.getFileName());
    }

    /**
     * 上传文件并构建文件信息的结构体
     *
     * @param file 文件本体
     * @return 单个文件上传后的信息结构
     */
    public FileUpload processFile(MultipartFile file,boolean local) throws Exception {
        // 1. 检查文件非空
        if (file.isEmpty()) {
            throw new DataNotComplianceException("文件不可为空");
        }

        // 2. 生成并验证文件名
        String fileName = Utils.generateUniqueFileName(file.getOriginalFilename());

        // 3. 保存文件到minio
        String filePath = saveFileToServer(file, fileName);

        // 4. 构建并返回 FileUpload 对象
        return buildFileUpload(file, fileName, filePath,local);
    }

    public ResponseEntity<Resource> getFile(String id) throws Exception {
        logger.info("getFileById {}", id);
        // 获取文件元数据
        FileUpload fileInfo = fileUploadInfoRepository.findById(id).orElseThrow(() -> new NotFoundException("文件信息不存在"));
        // 从Minio获取文件流
        InputStream fileStream = minioOperations.getFileStream(fileInfo.getFilePath());
        return Utils.exchangeInputStreamToResource(fileStream, fileInfo.getOriginalFileName());
    }

    public String getDownloadUrlFromTemp(String fileId) throws Exception{
        try {
            FileUpload file = fileUploadInfoRepository.findById(fileId).orElseThrow(() -> new NotFoundException("文件不存在"));
            String name = file.getFilePath();
            // 添加强制下载的响应头
            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("response-content-disposition",
                    "attachment; filename=\"" + URLEncoder.encode(file.getOriginalFileName(), "UTF-8") + "\"");
            logger.info(file.getOriginalFileName());
            return minioOperations.getDownloadUrl(name,180, reqParams);
        }catch (NotFoundException e){
            return fileService.getDownloadUrl(fileId);
        }

    }

    /**
     * 保存文件到服务器.
     *
     * @param file     需要保存的文件
     * @param fileName 文件名
     * @return 文件在minio的路径
     */
    private String saveFileToServer(MultipartFile file, String fileName) throws Exception {
        String name = minioOperations.createTempFileName(fileName);
        String contentType = file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType();
        try {
            minioOperations.uploadFile(name, file.getInputStream(), file.getSize(), contentType);
        } catch (Exception e) {
            logger.error("上传文件失败{}", name);
            //上传失败,文件删除回滚
            minioOperations.deleteFile(name);
            logger.info("文件上传失败，已删除回滚{}", name);
            throw new RuntimeException("上传文件失败");
        }
        logger.info("文件上传成功,文件名{},文件类型{},文件路径{}", fileName, file.getContentType(), name);
        return name;
    }

    /**
     * 构建上传后的文件信息结构.
     *
     * @param file     二进制文件
     * @param fileName 文件名
     * @param filePath 上传后的路径
     * @return 文件信息结构
     */
    private FileUpload buildFileUpload(MultipartFile file, String fileName, String filePath,boolean local) {
        logger.info("构建文件信息结构,文件名{},文件类型{},文件路径{}", fileName, file.getContentType(), filePath.replace("\\", "/"));
        FileUpload fileUpload = new FileUpload();
        fileUpload.setOriginalFileName(file.getOriginalFilename());
        fileUpload.setFileName(fileName);
        fileUpload.setFileType(file.getContentType());
        fileUpload.setFilePath(filePath.replace("\\", "/"));
        fileUpload.setUploadTime(Utils.getNowDate());
        fileUpload.setLocal(local);
        return fileUpload;
    }

    public FileUpload getFileUploadById(String id) {
        return fileUploadInfoRepository.findById(id).orElseThrow(() -> new NotFoundException("文件信息不存在"));
    }
}
