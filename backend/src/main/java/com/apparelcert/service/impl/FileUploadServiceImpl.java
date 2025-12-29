package com.apparelcert.service.impl;

import com.apparelcert.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 文件上传服务实现类
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadServiceImpl.class);

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.base-url:/uploads}")
    private String baseUrl;

    // 允许的图片类型
    private static final String[] IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    
    // 允许的文档类型
    private static final String[] DOCUMENT_TYPES = {"application/pdf", "application/msword", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
    
    // 允许的实操文件类型
    private static final String[] PRACTICAL_TYPES = {"image/jpeg", "image/png", "application/pdf",
        "application/zip", "application/x-rar-compressed", "video/mp4", "video/quicktime"};

    // 文件大小限制（MB）
    private static final Map<String, Long> SIZE_LIMITS = new HashMap<String, Long>() {{
        put("avatar", 5L);
        put("resume", 10L);
        put("practical", 100L);
        put("certificate", 10L);
        put("company", 20L);
    }};

    // 危险文件扩展名
    private static final Set<String> DANGEROUS_EXTENSIONS = new HashSet<>(Arrays.asList(
        "exe", "bat", "cmd", "sh", "ps1", "vbs", "js", "jar", "php", "asp", "aspx", "jsp"
    ));

    @Override
    public Map<String, Object> uploadFile(MultipartFile file, String type) {
        Map<String, Object> result = new HashMap<>();

        // 验证文件
        if (file == null || file.isEmpty()) {
            result.put("success", false);
            result.put("message", "文件不能为空");
            return result;
        }

        // 安全检查
        if (!securityCheck(file)) {
            result.put("success", false);
            result.put("message", "文件安全检查未通过");
            return result;
        }

        // 验证文件类型
        String[] allowedTypes = getAllowedTypes(type);
        if (!validateFileType(file, allowedTypes)) {
            result.put("success", false);
            result.put("message", "不支持的文件格式");
            return result;
        }

        // 验证文件大小
        long maxSize = SIZE_LIMITS.getOrDefault(type, 10L);
        if (!validateFileSize(file, maxSize)) {
            result.put("success", false);
            result.put("message", "文件大小超过限制（最大" + maxSize + "MB）");
            return result;
        }

        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + extension;

            // 创建目录
            String subDir = type + "/" + getDatePath();
            Path uploadDir = Paths.get(uploadPath, subDir);
            Files.createDirectories(uploadDir);

            // 保存文件
            Path filePath = uploadDir.resolve(newFilename);
            file.transferTo(filePath.toFile());

            // 返回结果
            String fileUrl = baseUrl + "/" + subDir + "/" + newFilename;
            result.put("success", true);
            result.put("url", fileUrl);
            result.put("filename", newFilename);
            result.put("originalFilename", originalFilename);
            result.put("size", file.getSize());
            result.put("message", "上传成功");

            logger.info("文件上传成功: {}", fileUrl);
        } catch (IOException e) {
            logger.error("文件上传失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "文件上传失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> uploadPracticalFile(MultipartFile file, Long certificationId) {
        Map<String, Object> result = uploadFile(file, "practical");
        
        if ((Boolean) result.get("success")) {
            result.put("certificationId", certificationId);
            // 可以在这里添加额外的业务逻辑，如记录到数据库
        }
        
        return result;
    }

    @Override
    public boolean validateFileType(MultipartFile file, String[] allowedTypes) {
        if (file == null || allowedTypes == null) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        for (String type : allowedTypes) {
            if (contentType.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean validateFileSize(MultipartFile file, long maxSizeMB) {
        if (file == null) {
            return false;
        }
        long maxBytes = maxSizeMB * 1024 * 1024;
        return file.getSize() <= maxBytes;
    }

    @Override
    public boolean securityCheck(MultipartFile file) {
        if (file == null) {
            return false;
        }

        // 检查文件扩展名
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }

        String extension = getFileExtension(filename).toLowerCase();
        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            logger.warn("检测到危险文件类型: {}", extension);
            return false;
        }

        // 检查文件名是否包含路径遍历字符
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            logger.warn("检测到路径遍历攻击: {}", filename);
            return false;
        }

        // 检查文件头（魔数）验证
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length > 0) {
                // 检查是否为可执行文件
                if (bytes.length >= 2 && bytes[0] == 0x4D && bytes[1] == 0x5A) {
                    logger.warn("检测到Windows可执行文件");
                    return false;
                }
                // 检查是否为ELF文件（Linux可执行文件）
                if (bytes.length >= 4 && bytes[0] == 0x7F && bytes[1] == 0x45 
                    && bytes[2] == 0x4C && bytes[3] == 0x46) {
                    logger.warn("检测到Linux可执行文件");
                    return false;
                }
            }
        } catch (IOException e) {
            logger.error("文件安全检查失败: {}", e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        try {
            // 从URL中提取文件路径
            String relativePath = fileUrl.replace(baseUrl, "");
            Path filePath = Paths.get(uploadPath, relativePath);
            
            File file = filePath.toFile();
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    logger.info("文件删除成功: {}", fileUrl);
                }
                return deleted;
            }
        } catch (Exception e) {
            logger.error("文件删除失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 根据类型获取允许的文件类型
     */
    private String[] getAllowedTypes(String type) {
        switch (type) {
            case "avatar":
            case "company":
                return IMAGE_TYPES;
            case "resume":
            case "certificate":
                return DOCUMENT_TYPES;
            case "practical":
                return PRACTICAL_TYPES;
            default:
                return IMAGE_TYPES;
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 获取日期路径（用于文件分目录存储）
     */
    private String getDatePath() {
        Calendar cal = Calendar.getInstance();
        return String.format("%d/%02d/%02d", 
            cal.get(Calendar.YEAR), 
            cal.get(Calendar.MONTH) + 1, 
            cal.get(Calendar.DAY_OF_MONTH));
    }
}
