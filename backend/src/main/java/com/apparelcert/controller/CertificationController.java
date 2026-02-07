package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Certification;
import com.apparelcert.entity.CertificationStandard;
import com.apparelcert.service.CertificationService;
import com.apparelcert.service.CertificationStandardService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 技能认证控制器
 */
@RestController
@RequestMapping("/api/personal/certification")
public class CertificationController {

    @Autowired
    private CertificationService certificationService;

    @Autowired
    private CertificationStandardService certificationStandardService;

    /**
     * 申请技能认证
     */
    @PostMapping
    public Result<Certification> applyCertification(@RequestBody Certification certification) {
        boolean result = certificationService.applyCertification(certification);
        return result ? Result.success(certification) : Result.error(500, "申请提交失败");
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

    /**
     * 获取可申请的认证标准列表（用户端）
     */
    @GetMapping("/standards")
    public Result<Page<CertificationStandard>> getAvailableStandards(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "100") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer level) {
        Page<CertificationStandard> pageInfo =
                certificationStandardService.pageQuery(page, size, keyword, type, level);
        return Result.success(pageInfo);
    }

    /**
     * 管理端更新认证状态（通过/拒绝）
     */
    @PutMapping("/admin/update-status")
    public Result<Boolean> updateCertificationStatus(@RequestBody Map<String, Object> params) {
        Long certificationId = Long.parseLong(params.get("certificationId").toString());
        Integer status = Integer.parseInt(params.get("status").toString());
        String reviewComment = params.get("reviewComment") != null ? params.get("reviewComment").toString() : null;

        boolean result;
        if (status != null && status == 3) {
            result = certificationService.completeCertification(certificationId, null, null, reviewComment);
        } else {
            result = certificationService.updateStatus(certificationId, status, reviewComment);
        }

        return result ? Result.success(true) : Result.error(500, "更新失败");
    }
}
