package org.example.ai_api.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.ai_api.Bean.Entity.Target;
import org.example.ai_api.Bean.Events.MinioFileDeleteEvent;
import org.example.ai_api.Bean.Events.MinioFileUploadedEvent;
import org.example.ai_api.Bean.Model.FileInfoFormSystem;
import org.example.ai_api.Bean.Model.FileUploadResult;
import org.example.ai_api.Exception.DataNotComplianceException;
import org.example.ai_api.Utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SystemFileService {
    private static final Logger logger = LoggerFactory.getLogger(SystemFileService.class);
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TargetService targetService;
    @Value("${systemFiles-upload}")
    private String fileLinkUpload;
    @Value("${systemFiles-fileInfo}")
    private String fileLinkFileInfo;
    @Value("${systemFiles-delete}")
    private String fileLinkDelete;

    /**
     * 获取所有文件信息
     *
     * @return List<FileInfoFormSystem>
     * @throws Exception 过程中的错误
     */
    @Cacheable(value = "allFiles", unless = "#result == null")
    public List<FileInfoFormSystem> getFileInfoFromSystem() throws Exception {
        String baseUrl = fileLinkFileInfo;
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl, String.class);
        String jsonBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper.readValue(
                jsonBody,
                new TypeReference<List<FileInfoFormSystem>>() {
                }
        );
    }

    /**
     * 上传文件到文件系统，完成清理缓存
     *
     * @param files  上传的文件列表
     * @param userId 用户id
     * @param target 上传的目标领域
     * @throws Exception 过程中的错误
     */
    @CacheEvict(value = {"allFiles", "filesByTarget"}, allEntries = true)
    public void publicFileUpload(List<MultipartFile> files, String userId, String target) throws Exception {
        List<byte[]> fileBytesList = new ArrayList<>();
        List<String> idsInSystem = new ArrayList<>();
        List<String> originalFileNames = new ArrayList<>();
        List<String> contentTypes = new ArrayList<>();
        for (MultipartFile file : files) {
            // 上传文件并记录数据
            FileUploadResult result = uploadSingleFile(file, target);
            // 收集数据到各个列表
            originalFileNames.add(result.getOriginalFileName());
            contentTypes.add(result.getContentType());
            fileBytesList.add(result.getFileBytes());
            idsInSystem.add(result.getFileId());
        }
        eventPublisher.publishEvent(new MinioFileUploadedEvent(fileBytesList, userId, target, true, idsInSystem, originalFileNames, contentTypes));
    }

    /**
     * 删除文件，完成清理缓存
     *
     * @param fileId 将删除的文件id
     * @param userId 操作者id
     * @param target 操作的目标领域
     */
    @CacheEvict(value = {"allFiles", "filesByTarget"}, allEntries = true)
    public void publicFileDelete(String fileId, String userId, String target) {
        //删除文件系统文件
        String baseUrl = fileLinkDelete;
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("ID", fileId)
                .build()
                .toUriString();
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                String.class
        );
        logger.info("删除文件{}返回{}", fileId, response.getBody());
        //触发事件删除minio文件
        eventPublisher.publishEvent(new MinioFileDeleteEvent(userId, fileId, target));
    }

    public void getFileFormSystem() throws Exception {
        String baseUrl = "http://files.luxshare-tech.com:8081/MajorFun/getFileByID?ID=88";
        ResponseEntity<byte[]> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                byte[].class
        );

    }

    /**
     * 构造上传文件到文件系统的请求体
     *
     * @param target    上传的目标领域
     * @param fileBytes 文件字节
     * @param filename  文件名
     * @return MultiValueMap<String, Object> 请求体结构
     */
    private MultiValueMap<String, Object> getMultiValueMap(String target, byte[] fileBytes, String filename) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        Map<String, Target> map = targetService.targetMap();
        body.add("Level", 3);
        body.add("Cid", map.get(target).getCid());
        body.add("UrlBase", map.get(target).getDir());
        body.add("CreateUser", "AI");
        Resource fileResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        body.add("FileLists0", fileResource);
        return body;
    }

    // 上传前的文件重名检查
    public void fileNameCheck(List<MultipartFile> files, List<String> fileNamesInSystem) throws Exception {
        //将上传文件名加入文件列表
        List<String> fileNames = files.stream()
                .map(MultipartFile::getOriginalFilename)
                .collect(Collectors.toList());
        //判断文件名是否重复
        if (Utils.checkDuplicateStringInList(fileNames)) {
            throw new DataNotComplianceException("上传文件存在文件名重复");
        }
        //判断文件名是否与文件系统中的文件名重复
        for (String fileName : fileNames) {
            if (fileNamesInSystem.contains(fileName)) {
                throw new DataNotComplianceException("上传文件存在文件与已有文件名重复");
            }
        }
    }

    // 处理单个文件上传
    private FileUploadResult uploadSingleFile(MultipartFile file, String target) throws Exception {
        // 1. 提取文件基本信息
        String originalFileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        byte[] fileBytes = file.getBytes();
        // 2. 上传文件到管理系统
        ResponseEntity<String> response = uploadToFileManagementSystem(fileBytes, target, originalFileName);
        logger.info("保存文件到管理系统，{}", response.getBody());
        // 3. 解析文件ID
        String fileId = extractFileId(response.getBody());
        logger.info("文件ID：{}", fileId != null ? fileId : "不存在");
        return new FileUploadResult(originalFileName, contentType, fileBytes, fileId);
    }

    // 上传文件到管理系统
    private ResponseEntity<String> uploadToFileManagementSystem(byte[] fileBytes, String target, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = getMultiValueMap(target, fileBytes, filename);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(
                fileLinkUpload,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
    }

    // 解析响应中的文件ID
    private String extractFileId(String responseBody) {
        try {
            JsonNode rootNode = new ObjectMapper().readTree(responseBody);
            if (rootNode.has("message") && rootNode.get("message").has("ID")) {
                return rootNode.get("message").get("ID").asText();
            }
        } catch (JsonProcessingException e) {
            logger.error("解析文件ID失败", e);
        }
        return null;
    }


}
