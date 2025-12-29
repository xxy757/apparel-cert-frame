package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Resume;
import com.apparelcert.service.ResumeExportService;
import com.apparelcert.service.ResumeService;
import com.apparelcert.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 简历控制器
 */
@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired(required = false)
    private ResumeExportService resumeExportService;

    /**
     * 获取当前用户简历
     */
    @GetMapping("/current")
    public Result<Map<String, Object>> getCurrentResume(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                return Result.error(401, "认证信息无效");
            }
            
            Map<String, Object> resumeData = resumeService.getResumeByUserId(userId);
            return Result.success(resumeData);
        } catch (Exception e) {
            return Result.error(500, "获取简历失败：" + e.getMessage());
        }
    }

    /**
     * 保存或更新简历
     */
    @PutMapping
    public Result<Boolean> saveResume(@RequestHeader("Authorization") String authHeader,
                                      @RequestBody Map<String, Object> resumeData) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                return Result.error(401, "认证信息无效");
            }
            
            boolean result = resumeService.saveResumeData(userId, resumeData);
            if (result) {
                return Result.success(true);
            } else {
                return Result.error(500, "保存简历失败");
            }
        } catch (Exception e) {
            return Result.error(500, "保存简历失败：" + e.getMessage());
        }
    }

    /**
     * 导出简历为PDF
     */
    @GetMapping("/export")
    public void exportResume(@RequestHeader("Authorization") String authHeader,
                             HttpServletResponse response) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                response.setStatus(401);
                return;
            }
            
            resumeService.exportToPdf(userId, response);
        } catch (Exception e) {
            response.setStatus(500);
        }
    }

    /**
     * 设置简历公开状态
     */
    @PutMapping("/public")
    public Result<Boolean> setPublicStatus(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody Map<String, Object> params) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                return Result.error(401, "认证信息无效");
            }
            
            Boolean isPublic = (Boolean) params.get("isPublic");
            boolean result = resumeService.setPublicStatusByUserId(userId, isPublic ? 1 : 0);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "设置公开状态失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID获取简历（供其他接口调用）
     */
    @GetMapping
    public Result<Resume> getResume(@RequestParam Long userId) {
        Resume resume = resumeService.getByUserId(userId);
        return Result.success(resume);
    }
    
    /**
     * 批量导出简历为Excel
     */
    @PostMapping("/batch-export/excel")
    public Result<String> batchExportToExcel(@RequestBody Map<String, Object> params) {
        if (resumeExportService == null) {
            return Result.error(500, "导出服务不可用");
        }
        
        @SuppressWarnings("unchecked")
        List<Long> resumeIds = ((List<Number>) params.get("resumeIds"))
            .stream().map(Number::longValue).collect(java.util.stream.Collectors.toList());
        
        if (resumeIds.isEmpty()) {
            return Result.error(400, "请选择要导出的简历");
        }
        
        String exportUrl = resumeExportService.batchExportToExcel(resumeIds);
        return exportUrl != null ? Result.success(exportUrl) : Result.error(500, "导出失败");
    }
    
    /**
     * 批量导出简历为PDF（ZIP打包）
     */
    @PostMapping("/batch-export/pdf")
    public Result<String> batchExportToPdf(@RequestBody Map<String, Object> params) {
        if (resumeExportService == null) {
            return Result.error(500, "导出服务不可用");
        }
        
        @SuppressWarnings("unchecked")
        List<Long> resumeIds = ((List<Number>) params.get("resumeIds"))
            .stream().map(Number::longValue).collect(java.util.stream.Collectors.toList());
        
        if (resumeIds.isEmpty()) {
            return Result.error(400, "请选择要导出的简历");
        }
        
        String exportUrl = resumeExportService.batchExportToPdf(resumeIds);
        return exportUrl != null ? Result.success(exportUrl) : Result.error(500, "导出失败");
    }
}
