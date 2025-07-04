package org.example.ai_api.Controller;

import org.example.ai_api.Bean.Entity.FileInfo;
import org.example.ai_api.Bean.Entity.KnowledgeFileInfo;
import org.example.ai_api.Bean.Entity.UserPermission;
import org.example.ai_api.Bean.Model.FileId;
import org.example.ai_api.Bean.Model.FileInfoFormSystem;
import org.example.ai_api.Bean.Model.ResultData;
import org.example.ai_api.Bean.WebRequest.KnowledgeBase;
import org.example.ai_api.Exception.BadRequestException;
import org.example.ai_api.Exception.NotAccessedException;
import org.example.ai_api.Service.*;
import org.example.ai_api.Utils.Utils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;

/**
 * 文件信息管理接口.
 *
 * @author 10353965
 */
@RestController
@RequestMapping("/Files")
//知识库文件管理接口
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class.getName());
    @Value("${systemFiles-view}")
    private String fileLink;
    @Autowired
    private FileService fileService;
    @Autowired
    private FileUploadInfoService fileUploadInfoService;
    @Autowired
    private UserPermissionService userPermissionService;
    @Autowired
    private SystemFileService systemFileService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 根据文件名获得文件链接.
     *
     * @param requestFileInfo the request file info
     * @return the file link by name
     */
    @PostMapping("/getFileInfoByName")
    @ResponseBody
    public ResultData<FileInfo> getFileLinkByName(@RequestBody FileInfo requestFileInfo) {
        logger.info("getFileLinkByName{}", requestFileInfo.getFileName());
        FileInfo fileInfo = fileService.findByFileName(requestFileInfo.getFileName());
        return ResultData.success(fileInfo);
    }

    /**
     * 根据id获得文件文本
     *
     * @param id 文件id
     * @return 文件文本
     * @throws Exception 异常
     */
    @PostMapping("/getContentById")
    @ResponseBody
    public ResultData<String> getContentById(@RequestParam("id") String id) throws Exception {
        logger.info("getContentById{}", id);
        String content = fileUploadInfoService.getContentById(id);
        logger.info("getContentById{}", content);
        return ResultData.success("获取成功", content);
    }

    /**
     * 根据id获得二进制文件.
     *
     * @param id 文件id
     * @return 根据文件信息构造的二进制文件
     * @throws Exception 异常
     */
    @PostMapping("/getFileById")
    public ResponseEntity<Resource> getFileById(@RequestBody FileId id) throws Exception {
        logger.info("getFileById{}", id);
        if(id.isLocal()){
            return fileUploadInfoService.getFile(id.getFileId());
        }else {
            return fileService.getKnowledgeFileById(id.getFileId());
        }
    }

    /**
     * 根据id获得二进制知识库文件.
     *
     * @param id 文件id
     * @return 根据文件信息构造的二进制文件
     * @throws Exception 异常
     */
    @PostMapping("/knowledgeFileById")
    public ResponseEntity<Resource> knowledgeFileById(@RequestParam("id") String id) throws Exception {
        logger.info("getKnowledgeFileById{}", id);
        return fileService.getKnowledgeFileById(id);
    }

    /**
     * 知识库文件上传
     *
     * @param files  上传文件列表
     * @param userId 上传者id
     * @param target 上传文件所属知识库领域
     * @return 上传后的文件信息列表
     * @throws Exception 异常
     */
    @PostMapping("/knowledgeFileUpload")
    public ResultData<Void> upload(@RequestPart("file") List<MultipartFile> files, @RequestParam("userId") String userId, @RequestParam("target") String target, @RequestParam("isPublic") boolean isPublic) throws Exception {
        logger.info("upload:{}", userId);
        if (isPublic) {
            if (!userPermissionService.checkUserPermission(userId, target).isUpload()) {
                throw new NotAccessedException("无权限访问该领域");
            }
            // 获取当前已存在的文件列表
            List<String> fileInSystem = fileService.getFileByTarget(target).stream().map(FileInfoFormSystem::getCategory).collect(Collectors.toList());
            // 上传前检查文件名是否重复
            systemFileService.fileNameCheck(files, fileInSystem);
            // 上传文件到管理系统
            systemFileService.publicFileUpload(files, userId, target);
        } else {
            List<KnowledgeFileInfo> knowledgeFileInfoList = fileService.knowledgeFileUpload(files, userId, target, false);
            List<KnowledgeFileInfo> result = fileService.saveAll(knowledgeFileInfoList);
            fileService.buildFileConversionTask(result);
            // 异步调用转换服务完成转换
            fileService.covertPrivateKnowledgeFiles(result);
        }
        return ResultData.success("上传成功");
    }

    /**
     * 知识库文件删除
     *
     * @param knowledgeBase 相关请求体
     * @return 删除结果
     * @throws Exception 无操作权限或代码运行错误时抛出异常
     */
    @PostMapping("/knowledgeFileDelete")
    public ResultData<String> delete(@RequestBody KnowledgeBase<String> knowledgeBase) throws Exception {
        String userId = knowledgeBase.getUserId();
        String target = knowledgeBase.getTarget();
        boolean isPublic = knowledgeBase.isPublic();
        List<String> fileIds = knowledgeBase.getFile();
        logger.info("delete:{}", userId);
        //前置权限检查
        if (isPublic) {
            //权限检查
            if (!userPermissionService.checkUserPermission(userId, target).isDelete()) {
                throw new NotAccessedException("无权限访问该领域");
            }
        }
        if (!isPublic) {
            for (String fileId : fileIds) {
                fileService.knowledgeFileDelete(fileId, userId);
            }
        } else {
            for (String fileId : fileIds) {
                systemFileService.publicFileDelete(fileId, userId, target);
            }
        }
        return ResultData.success("删除成功");
    }

    /**
     * 权限检查
     *
     * @param userId 用户id
     * @return 用户有哪些领域的权限
     */
    @PostMapping("/permissionCheck")
    public ResultData<List<UserPermission>> permissionCheck(@RequestParam("userId") String userId) {
        logger.info("permissionCheck:{}", userId);
        List<UserPermission> permissionList = userPermissionService.getUserPermissionListByUserId(userId);
        //法务知识库先隐藏
        permissionList.removeIf(userPermission -> userPermission.getTarget().equals("Law"));
        return ResultData.success("操作成功", permissionList);
    }

    /**
     * 根据用户id获得文件信息列表.
     *
     * @param knowledgeBase 请求结构体
     * @return 文件信息列表
     */
    @PostMapping("/getFileListByUserId")
    public ResultData<Page<KnowledgeFileInfo>> getFileListByUserId(@RequestBody KnowledgeBase<Void> knowledgeBase) {
        logger.info("getFileListByUserId:{}", knowledgeBase);
        List<KnowledgeFileInfo> fileInfos = fileService.searchFile(knowledgeBase.getKeywords(), knowledgeBase.getTarget(), knowledgeBase.getUserId(), knowledgeBase.isPublic());
        if (knowledgeBase.getSortType() != null) {
            fileInfos = fileService.sortFileList(fileInfos, knowledgeBase.getSortType(), knowledgeBase.isIncrease());
        }
        return ResultData.success("操作成功", Utils.getFilesPage(knowledgeBase.getPage() - 1, knowledgeBase.getPageSize(), fileInfos));
    }

    /**
     * 根据需求排序文件列表.
     *
     * @param fileList 待排序文件列表
     * @param sortType 排序方式
     * @return 排序处理后的列表
     */
    @PostMapping("/sortFileList")
    public ResultData<List<KnowledgeFileInfo>> sortFileList(@RequestParam("fileList") List<KnowledgeFileInfo> fileList, @RequestParam("sortType") String sortType) {
        logger.info("sortType:{}", sortType);
        fileList = fileService.sortFileList(fileList, sortType, true);
        return ResultData.success("操作成功", fileList);
    }

    /**
     * 搜索文件信息
     *
     * @param userId   用户id
     * @param target   目标领域
     * @param keyword  搜索关键字
     * @param isPublic 是否公开
     * @return 搜索结果
     */
    @PostMapping("/searchFile")
    public ResultData<List<KnowledgeFileInfo>> searchFile(@RequestParam("userId") String userId, @RequestParam("target") String target, @RequestParam("keyword") String keyword, @RequestParam("isPublic") boolean isPublic) {
        logger.info("searchFile:{}", keyword);
        List<KnowledgeFileInfo> fileInfos = fileService.searchFile(keyword, target, userId, isPublic);
        return ResultData.success("操作成功", fileInfos);
    }

    /**
     * 获取知识库文件下载链接(三分钟有效)
     *
     * @param userId 用户名
     * @param fileId 文件id
     * @return 下载的url
     * @throws Exception 异常
     */
    @PostMapping("/getDownloadUrl")
    public ResultData<String> getDownloadUrl(@RequestParam("userId") String userId, @RequestParam("fileId") String fileId) throws Exception {
        if (userId == null || fileId == null) {
            throw new BadRequestException("用户id或文件id不可为空");
        }
        fileService.checkUserPermissionForFile(fileId, userId);
        String downloadUrl = fileService.getDownloadUrl(fileId);
        return ResultData.success("操作成功", downloadUrl);
    }

    /**
     * 获取临时文件下载链接 (三分钟有效)
     * @param fileId 文件id
     * @return 下载的url
     */
    @PostMapping("getDownloadUrlFromTemp")
    public ResultData<String> getDownloadUrlFromTemp(@RequestParam("fileId") String fileId) throws Exception {
        if (fileId == null) {
            throw new BadRequestException("文件id不可为空");
        }
        String downloadUrl = fileUploadInfoService.getDownloadUrlFromTemp(fileId);
        return ResultData.success("操作成功", downloadUrl);
    }

    /**
     * 获取文件信息列表(从文件管理系统中获取)
     *
     * @param userId 用户id
     * @param target 目标领域
     * @return 文件信息列表
     * @throws Exception 异常
     */
    @PostMapping("/getFileInfoFromSystem")
    public ResultData<Page<KnowledgeFileInfo>> getFileInfoFromSystem(
            @RequestParam("userId") String userId,
            @RequestParam("target") String target,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", defaultValue = "") String keyword
    ) throws Exception {
        logger.info("用户{}查看知识库{}", userId, target);
        List<FileInfoFormSystem> fileInfo = fileService.getFileByTarget(target);
        List<KnowledgeFileInfo> knowledgeFileInfos = fileService.changeToKnowledgeFile(fileInfo);
        List<KnowledgeFileInfo> result;
        if ("".equals(keyword)) {
            result = knowledgeFileInfos;
        } else {
            result = knowledgeFileInfos.stream()
                    .filter(fileInfoFormSystem -> fileInfoFormSystem.getFileName().contains(keyword))
                    .collect(Collectors.toList());
        }
        result = result.stream()
                .sorted(Comparator.comparing(KnowledgeFileInfo::getCreateTime).reversed())
                .collect(Collectors.toList());
        logger.info("文件列表长度:{}", result.size());
        return ResultData.success("操作成功", Utils.getFilesPage(page - 1, size, result));
    }

    /**
     * 刷新文件列表缓存
     *
     * @return 操作结果
     */
    @PostMapping("/refreshSystemFileListCache")
    public ResultData<String> refreshSystemFileListCache() {
        cacheService.refreshSystemFileListCache();
        return ResultData.success("操作成功");
    }

    /**
     * 获取文件预览连接
     *
     * @param fileName 文件名
     * @param target   所属领域
     * @return 文件预览连接
     * @throws Exception 异常
     */
    @PostMapping("/getFileLinkByName")
    public ResultData<String> getFileLink(@RequestParam("fileName") String fileName, @RequestParam("target") String target) throws Exception {
        if (fileName == null || target == null) {
            throw new BadRequestException("文件名或目标领域不可为空");
        }
        List<FileInfoFormSystem> fileInfoFormSystems = fileService.getFileByTarget(target);
        for (FileInfoFormSystem fileInfoFormSystem : fileInfoFormSystems) {
            String name = Utils.removeFileExtension(fileInfoFormSystem.getCategory());
            if (fileName.equals(name)) {
                return ResultData.success("操作成功", fileLink + fileInfoFormSystem.getFileKey());
            }
        }
        throw new BadRequestException("文件名不存在");
    }

    // todo:将文件服务器的文件信息同步到minio
    @PostMapping("/test")
    public ResultData<Void> test() {

        return ResultData.success("操作成功");
    }
}
