package com.apparelcert.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {

    /**
     * 上传文件
     * @param file 文件
     * @param type 文件类型（avatar/resume/practical/certificate/company）
     * @return 上传结果，包含文件URL
     */
    Map<String, Object> uploadFile(MultipartFile file, String type);

    /**
     * 上传实操成果文件
     * @param file 文件
     * @param certificationId 认证ID
     * @return 上传结果
     */
    Map<String, Object> uploadPracticalFile(MultipartFile file, Long certificationId);

    /**
     * 验证文件格式
     * @param file 文件
     * @param allowedTypes 允许的文件类型
     * @return 是否有效
     */
    boolean validateFileType(MultipartFile file, String[] allowedTypes);

    /**
     * 验证文件大小
     * @param file 文件
     * @param maxSizeMB 最大大小（MB）
     * @return 是否有效
     */
    boolean validateFileSize(MultipartFile file, long maxSizeMB);

    /**
     * 安全检查文件
     * @param file 文件
     * @return 是否安全
     */
    boolean securityCheck(MultipartFile file);

    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);
}
