package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Certificate;
import com.apparelcert.service.CertificateService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 验证证书真实性（公开接口）
     */
    @GetMapping("/verify")
    public Result<Map<String, Object>> verifyCertificate(@RequestParam String certificateNumber) {
        Map<String, Object> result = new HashMap<>();
        
        Certificate certificate = certificateService.getByCertificateNumber(certificateNumber);
        if (certificate == null) {
            result.put("valid", false);
            result.put("message", "证书不存在");
            return Result.success(result);
        }
        
        boolean isValid = certificateService.verifyCertificate(certificateNumber);
        result.put("valid", isValid);
        
        if (isValid) {
            result.put("message", "证书有效");
            result.put("certificateNumber", certificate.getCertificateNumber());
            result.put("name", certificate.getName());
            result.put("certificationType", certificate.getCertificationType());
            result.put("level", certificate.getLevel());
            result.put("issueDate", certificate.getIssueDate());
            result.put("expireDate", certificate.getExpireDate());
            result.put("issuer", certificate.getIssuer());
            result.put("description", certificate.getDescription());
        } else {
            if (certificate.getExpireDate() != null && certificate.getExpireDate().before(new Date())) {
                result.put("message", "证书已过期");
            } else if (!"有效".equals(certificate.getCertificateStatus())) {
                result.put("message", "证书已失效");
            } else {
                result.put("message", "证书无效");
            }
        }
        
        return Result.success(result);
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
    
    /**
     * 检查证书有效期
     */
    @GetMapping("/validity")
    public Result<Map<String, Object>> checkCertificateValidity(@RequestParam Long certificateId) {
        Map<String, Object> validity = certificateService.checkCertificateValidity(certificateId);
        return Result.success(validity);
    }
    
    /**
     * 获取即将过期的证书
     */
    @GetMapping("/expiring")
    public Result<List<Certificate>> getExpiringCertificates(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "30") Integer daysBeforeExpire) {
        List<Certificate> certificates = certificateService.getExpiringCertificates(userId, daysBeforeExpire);
        return Result.success(certificates);
    }
    
    /**
     * 续期证书
     */
    @PostMapping("/renew")
    public Result<Boolean> renewCertificate(
            @RequestParam Long certificateId,
            @RequestParam(defaultValue = "1") Integer years) {
        boolean result = certificateService.renewCertificate(certificateId, years);
        return result ? Result.success(true) : Result.error(500, "续期失败");
    }
    
    /**
     * 生成证书分享链接
     */
    @GetMapping("/share-link")
    public Result<String> generateShareLink(@RequestParam Long certificateId) {
        String shareLink = certificateService.generateShareLink(certificateId);
        return shareLink != null ? Result.success(shareLink) : Result.error(404, "证书不存在");
    }
    
    /**
     * 通过分享码获取证书信息
     */
    @GetMapping("/share/{shareCode}")
    public Result<Map<String, Object>> getCertificateByShareCode(@PathVariable String shareCode) {
        Map<String, Object> result = certificateService.getCertificateByShareCode(shareCode);
        Boolean success = (Boolean) result.get("success");
        if (success != null && success) {
            return Result.success(result);
        } else {
            return Result.error(404, (String) result.get("message"));
        }
    }
}