package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Resume;
import com.apparelcert.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 简历控制器
 */
@RestController
@RequestMapping("/api/personal/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    /**
     * 获取用户简历
     */
    @GetMapping
    public Result<Resume> getResume(@RequestParam Long userId) {
        Resume resume = resumeService.getByUserId(userId);
        return Result.success(resume);
    }

    /**
     * 保存或更新简历
     */
    @PostMapping
    public Result<Boolean> saveOrUpdateResume(@RequestBody Resume resume) {
        boolean result = resumeService.saveOrUpdateResume(resume);
        return Result.success(result);
    }

    /**
     * 导出简历为PDF
     */
    @GetMapping("/export")
    public Result<String> exportResume(@RequestParam Long resumeId) {
        String pdfUrl = resumeService.exportToPdf(resumeId);
        return Result.success(pdfUrl);
    }

    /**
     * 设置简历公开状态
     */
    @PutMapping("/public")
    public Result<Boolean> setPublicStatus(@RequestParam Long resumeId, @RequestParam Integer isPublic) {
        boolean result = resumeService.setPublicStatus(resumeId, isPublic);
        return Result.success(result);
    }
}