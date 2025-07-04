package org.example.ai_api.Utils;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * minio相关操作封装
 */
@Component
public class MinioOperations {
    @Autowired
    private MinioClient minioClient;
    @Value("${minio.bucketName}")
    private String bucketName;

    /**上传文件到minio
     *
     * @param name minio文件名
     * @param stream 文件流
     * @param size 文件大小
     * @param contentType 文件类型
     * @throws Exception 抛出错误
     */
    public void uploadFile(String name, InputStream stream, long size, String contentType) throws Exception{
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(name)
                        .stream(stream, size, -1)
                        .contentType(contentType)
                        .build()
        );
    }

    /** 从minio删除文件
     *
     * @param name minio文件名
     * @throws Exception 抛出错误
     */
    public void deleteFile(String name) throws Exception{
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(name)
                        .build()
        );
    }

    /**获得minio文件下载链接
     *
     * @param name minio文件名
     * @param time 链接可用时长(以秒为单位)
     * @param reqParams 定义请求头参数
     * @return 下载链接
     * @throws Exception 抛出错误
     */
    public String getDownloadUrl(String name, int time, Map<String, String> reqParams) throws Exception{
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(name)
                        .expiry(time, TimeUnit.SECONDS)
                        .extraQueryParams(reqParams)
                        .build()
        );
    }

    /** 获得minio文件流
     *
     * @param name minio文件名
     * @return 文件流
     * @throws Exception 抛出错误
     */
    public InputStream getFileStream(String name) throws Exception{
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(name)
                        .build()
        );
    }

    /** 获得minio文件元数据
     *
     * @param name minio文件名
     * @return 文件元数据
     * @throws Exception 抛出错误
     */
    public StatObjectResponse getObjectStat(String name) throws Exception{
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(name)
                        .build()
        );
    }

    /**
     * 根据相关信息生成知识库文件在minio的文件名
     *
     * @param fileName 原文件名
     * @param target   目标领域
     * @param userId   上传者id
     * @param isPublic 是否公有
     * @return 知识库文件在minio的文件名
     */
    public String createKnowledgeFileName(String fileName, String target, String userId, boolean isPublic){
        String prefix = isPublic ? "public/" : "private/";
        // 规范路径格式（确保以/结尾）
        String normalizedPath;
        if (isPublic) {
            normalizedPath = target.endsWith("/") ? target : target + "/";
        } else {
            normalizedPath = userId.endsWith("/") ? userId : userId + "/";
        }
        return prefix + normalizedPath + fileName;
    }

    /**
     * 创建临时文件在minio的文件名
     * @param fileName 文件原名
     * @return 临时文件在minio的文件名
     */
    public String createTempFileName(String fileName){
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return "tmp" + "/" + dateDir + "/" + fileName;
    }
}
