package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Certification;
import com.apparelcert.service.CertificationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 技能认证控制器
 */
@RestController
@RequestMapping("/api/personal/certification")
public class CertificationController {

    @Autowired
    private CertificationService certificationService;

    /**
     * 申请技能认证
     */
    @PostMapping
    public Result<Boolean> applyCertification(@RequestBody Certification certification) {
        boolean result = certificationService.applyCertification(certification);
        return Result.success(result);
    }

    /**
     * 获取用户认证列表
     */
    @GetMapping("/list")
    public Result<List<Certification>> getCertificationList(@RequestParam Long userId) {
        List<Certification> list = certificationService.getByUserId(userId);
        return Result.success(list);
    }

    /**
     * 获取认证详情
     */
    @GetMapping("/detail")
    public Result<Certification> getCertificationDetail(@RequestParam Long id) {
        Certification certification = certificationService.getById(id);
        return Result.success(certification);
    }

    /**
     * 上传实操成果
     */
    @PutMapping("/upload")
    public Result<Boolean> uploadPractical(@RequestParam Long certificationId, @RequestParam String fileUrl) {
        boolean result = certificationService.uploadPractical(certificationId, fileUrl);
        return Result.success(result);
    }

    /**
     * 获取认证进度
     */
    @GetMapping("/progress")
    public Result<Integer> getCertificationProgress(@RequestParam Long certificationId) {
        Integer progress = certificationService.getProgress(certificationId);
        return Result.success(progress);
    }

    /**
     * 分页查询认证记录
     */
    @GetMapping("/page")
    public Result<Page<Certification>> pageQuery(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) Long userId) {
        Page<Certification> pageInfo = certificationService.pageQuery(page, size, userId);
        return Result.success(pageInfo);
    }
}