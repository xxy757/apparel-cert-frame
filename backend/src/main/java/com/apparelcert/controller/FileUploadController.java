package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.service.CertificationService;
import com.apparelcert.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired(required = false)
    private CertificationService certificationService;

    /**
     * 通用文件上传
     * @param file 文件
     * @param type 文件类型（avatar/resume/practical/certificate/company）
     */
    @PostMapping
    public Result<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "avatar") String type) {
        Map<String, Object> result = fileUploadService.uploadFile(file, type);
        Boolean success = (Boolean) result.get("success");
        if (success != null && success) {
            return Result.success(result);
        } else {
            return Result.error(400, (String) result.get("message"));
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = fileUploadService.uploadFile(file, "avatar");
        Boolean success = (Boolean) result.get("success");
        if (success != null && success) {
            return Result.success(result);
        } else {
            return Result.error(400, (String) result.get("message"));
        }
    }

    /**
     * 上传简历附件
     */
    @PostMapping("/resume")
    public Result<Map<String, Object>> uploadResume(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = fileUploadService.uploadFile(file, "resume");
        Boolean success = (Boolean) result.get("success");
        if (success != null && success) {
            return Result.success(result);
        } else {
            return Result.error(400, (String) result.get("message"));
        }
    }

    /**
     * 上传实操成果文件
     * 上传成功后自动关联到认证申请记录
     */
    @PostMapping("/practical")
    public Result<Map<String, Object>> uploadPractical(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long certificationId) {
        // 1. 上传文件
        Map<String, Object> result = fileUploadService.uploadPracticalFile(file, certificationId);
        Boolean success = (Boolean) result.get("success");

        if (success != null && success) {
            // 2. 文件上传成功，更新认证记录
            if (certificationService != null) {
                String fileUrl = (String) result.get("url");
                boolean updated = certificationService.uploadPractical(certificationId, fileUrl);
                if (updated) {
                    log.info("实操作品上传成功并已关联认证记录: certificationId={}, fileUrl={}", certificationId, fileUrl);
                    result.put("certificationUpdated", true);
                } else {
                    log.warn("实操作品上传成功但更新认证记录失败: certificationId={}", certificationId);
                    result.put("certificationUpdated", false);
                    result.put("warning", "文件上传成功，但关联认证记录失败，请联系管理员");
                }
            }
            return Result.success(result);
        } else {
            return Result.error(400, (String) result.get("message"));
        }
    }

    /**
     * 上传企业形象图片（LOGO/Banner/Gallery）
     */
    @PostMapping("/company")
    public Result<Map<String, Object>> uploadCompanyImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = fileUploadService.uploadFile(file, "company");
        Boolean success = (Boolean) result.get("success");
        if (success != null && success) {
            return Result.success(result);
        } else {
            return Result.error(400, (String) result.get("message"));
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping
    public Result<Boolean> deleteFile(@RequestParam String fileUrl) {
        boolean result = fileUploadService.deleteFile(fileUrl);
        return result ? Result.success(true) : Result.error(500, "删除文件失败");
    }
}
