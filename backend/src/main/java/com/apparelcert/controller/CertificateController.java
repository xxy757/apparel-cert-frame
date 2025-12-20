package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Certificate;
import com.apparelcert.service.CertificateService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 证书控制器
 */
@RestController
@RequestMapping("/api/admin/certification/certificate")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    /**
     * 分页查询证书列表
     */
    @GetMapping
    public Result<Page<Certificate>> getCertificateList(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String certificationType,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String status) {
        Page<Certificate> pageInfo = certificateService.pageQuery(page, size, keyword, certificationType, level, status);
        return Result.success(pageInfo);
    }

    /**
     * 获取证书详情
     */
    @GetMapping("/detail")
    public Result<Certificate> getCertificateDetail(@RequestParam Long certificateId) {
        Certificate certificate = certificateService.getById(certificateId);
        return Result.success(certificate);
    }

    /**
     * 保存或更新证书
     */
    @PostMapping
    public Result<Boolean> saveOrUpdateCertificate(@RequestBody Certificate certificate) {
        boolean result = certificateService.saveOrUpdateCertificate(certificate);
        return Result.success(result);
    }

    /**
     * 删除证书
     */
    @DeleteMapping
    public Result<Boolean> deleteCertificate(@RequestParam Long certificateId) {
        boolean result = certificateService.deleteCertificate(certificateId);
        return Result.success(result);
    }

    /**
     * 批量删除证书
     */
    @DeleteMapping("/batch")
    public Result<Boolean> deleteBatchCertificates(@RequestBody List<Long> ids) {
        boolean result = certificateService.deleteBatchCertificates(ids);
        return Result.success(result);
    }

    /**
     * 生成证书
     */
    @PostMapping("/generate")
    public Result<Certificate> generateCertificate(@RequestParam Long certificationId) {
        Certificate certificate = certificateService.generateCertificate(certificationId);
        return Result.success(certificate);
    }

    /**
     * 验证证书真实性
     */
    @GetMapping("/verify")
    public Result<Boolean> verifyCertificate(@RequestParam String certificateNumber) {
        boolean isValid = certificateService.verifyCertificate(certificateNumber);
        return Result.success(isValid);
    }

    /**
     * 根据证书编号获取证书
     */
    @GetMapping("/by-number")
    public Result<Certificate> getByCertificateNumber(@RequestParam String certificateNumber) {
        Certificate certificate = certificateService.getByCertificateNumber(certificateNumber);
        return Result.success(certificate);
    }

    /**
     * 获取用户的所有证书
     */
    @GetMapping("/by-user")
    public Result<List<Certificate>> getByUserId(@RequestParam Long userId) {
        List<Certificate> certificates = certificateService.getByUserId(userId);
        return Result.success(certificates);
    }

    /**
     * 更新证书状态
     */
    @PutMapping("/status")
    public Result<Boolean> updateCertificateStatus(@RequestParam Long certificateId, @RequestParam String status) {
        boolean result = certificateService.updateCertificateStatus(certificateId, status);
        return Result.success(result);
    }

    /**
     * 导出证书为PDF
     */
    @GetMapping("/export")
    public Result<String> exportCertificate(@RequestParam Long certificateId) {
        String pdfUrl = certificateService.exportCertificateToPdf(certificateId);
        return Result.success(pdfUrl);
    }
}