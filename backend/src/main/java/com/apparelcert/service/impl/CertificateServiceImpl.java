package com.apparelcert.service.impl;

import com.apparelcert.entity.Certificate;
import com.apparelcert.entity.Certification;
import com.apparelcert.mapper.CertificateMapper;
import com.apparelcert.service.CertificateService;
import com.apparelcert.service.CertificationService;
import com.apparelcert.service.ResumeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 证书服务实现类
 */
@Service
public class CertificateServiceImpl extends ServiceImpl<CertificateMapper, Certificate> implements CertificateService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateServiceImpl.class);

    @Autowired
    private CertificateMapper certificateMapper;
    
    @Autowired
    @Lazy
    private CertificationService certificationService;
    
    @Autowired(required = false)
    private ResumeService resumeService;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public Page<Certificate> pageQuery(Integer page, Integer size, String keyword, String certificationType, Integer level, String status) {
        Page<Certificate> pageInfo = new Page<>(page, size);
        QueryWrapper<Certificate> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword).or().like("certificate_number", keyword);
        }
        if (certificationType != null && !certificationType.isEmpty()) {
            wrapper.eq("certification_type", certificationType);
        }
        if (level != null) {
            wrapper.eq("level", level);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq("certificate_status", status);
        }

        wrapper.orderByDesc("issue_date");
        return certificateMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public Certificate getById(Long id) {
        return certificateMapper.selectById(id);
    }

    @Override
    public boolean saveOrUpdateCertificate(Certificate certificate) {
        return this.saveOrUpdate(certificate);
    }

    @Override
    public boolean deleteCertificate(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean deleteBatchCertificates(List<Long> ids) {
        return this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Certificate generateCertificate(Long certificationId) {
        // 获取认证信息
        Certification certification = certificationService.getById(certificationId);
        if (certification == null) {
            return null;
        }
        
        // 检查是否已生成证书
        QueryWrapper<Certificate> existWrapper = new QueryWrapper<>();
        existWrapper.eq("user_id", certification.getUserId());
        existWrapper.eq("certification_type", certification.getCertificationType());
        existWrapper.eq("level", certification.getLevel());
        Certificate existing = certificateMapper.selectOne(existWrapper);
        if (existing != null) {
            return existing; // 已存在证书
        }
        
        // 获取用户信息
        String userName = "持证人";
        String idCard = "";
        if (resumeService != null) {
            Map<String, Object> resumeData = resumeService.getResumeByUserId(certification.getUserId());
            if (resumeData != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> basicInfo = (Map<String, Object>) resumeData.get("basicInfo");
                if (basicInfo != null) {
                    userName = (String) basicInfo.getOrDefault("name", "持证人");
                    idCard = (String) basicInfo.getOrDefault("idCard", "");
                }
            }
        }
        
        // 生成唯一证书编号
        String certificateNumber = generateCertificateNumber(certification.getCertificationType(), certification.getLevel());
        
        // 生成证书
        Certificate certificate = new Certificate();
        certificate.setUserId(certification.getUserId());
        certificate.setCertificationType(certification.getCertificationType());
        certificate.setLevel(certification.getLevel());
        certificate.setCertificateNumber(certificateNumber);
        certificate.setName(userName);
        certificate.setIdCard(idCard);
        certificate.setIssueDate(new Date());
        certificate.setExpireDate(new Date(System.currentTimeMillis() + 3L * 365 * 24 * 60 * 60 * 1000)); // 有效期3年
        certificate.setCertificateStatus("有效");
        certificate.setIssuer("服装行业人才技能认证中心");
        certificate.setDescription(getCertificationDescription(certification.getCertificationType(), certification.getLevel()));
        
        // 生成二维码URL
        String qrCodeUrl = generateQrCodeUrl(certificateNumber);
        certificate.setQrCodeUrl(qrCodeUrl);
        
        this.save(certificate);
        
        // 生成证书PDF
        String pdfUrl = generateCertificatePdf(certificate);
        certificate.setCertificateUrl(pdfUrl);
        this.updateById(certificate);
        
        // 同步认证信息到简历
        syncCertificationToResume(certification.getUserId(), certificate);
        
        logger.info("证书生成成功: {}", certificateNumber);
        return certificate;
    }

    @Override
    public boolean verifyCertificate(String certificateNumber) {
        Certificate certificate = this.getByCertificateNumber(certificateNumber);
        if (certificate == null) {
            return false;
        }
        // 检查证书状态和有效期
        return "有效".equals(certificate.getCertificateStatus()) 
            && certificate.getExpireDate() != null 
            && certificate.getExpireDate().after(new Date());
    }

    @Override
    public Certificate getByCertificateNumber(String certificateNumber) {
        QueryWrapper<Certificate> wrapper = new QueryWrapper<>();
        wrapper.eq("certificate_number", certificateNumber);
        return certificateMapper.selectOne(wrapper);
    }

    @Override
    public List<Certificate> getByUserId(Long userId) {
        QueryWrapper<Certificate> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("issue_date");
        return certificateMapper.selectList(wrapper);
    }

    @Override
    public boolean updateCertificateStatus(Long certificateId, String status) {
        Certificate certificate = new Certificate();
        certificate.setId(certificateId);
        certificate.setCertificateStatus(status);
        return this.updateById(certificate);
    }

    @Override
    public String exportCertificateToPdf(Long certificateId) {
        Certificate certificate = this.getById(certificateId);
        if (certificate == null) {
            return null;
        }
        
        // 如果已有PDF，直接返回
        if (certificate.getCertificateUrl() != null && !certificate.getCertificateUrl().isEmpty()) {
            return certificate.getCertificateUrl();
        }
        
        // 生成新的PDF
        return generateCertificatePdf(certificate);
    }
    
    /**
     * 生成唯一证书编号
     * 格式：AC-类型代码-年份-6位序号
     */
    private String generateCertificateNumber(String certificationType, Integer level) {
        String typeCode = getTypeCode(certificationType);
        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        String levelCode = String.format("%02d", level != null ? level : 1);
        String sequence = String.format("%06d", System.currentTimeMillis() % 1000000);
        return "AC-" + typeCode + "-" + year + levelCode + "-" + sequence;
    }
    
    /**
     * 获取认证类型代码
     */
    private String getTypeCode(String certificationType) {
        if (certificationType == null) return "XX";
        switch (certificationType) {
            case "服装设计": return "SD";
            case "服装制版": return "PM";
            case "服装工艺": return "PC";
            case "服装营销": return "MK";
            case "服装陈列": return "VM";
            case "服装质检": return "QC";
            default: return "XX";
        }
    }
    
    /**
     * 获取认证描述
     */
    private String getCertificationDescription(String certificationType, Integer level) {
        String levelName = "";
        if (level != null) {
            switch (level) {
                case 1: levelName = "初级"; break;
                case 2: levelName = "中级"; break;
                case 3: levelName = "高级"; break;
                case 4: levelName = "专家级"; break;
                default: levelName = "";
            }
        }
        return "服装行业" + (certificationType != null ? certificationType : "") + levelName + "技能认证证书";
    }
    
    /**
     * 生成二维码URL（用于证书验证）
     */
    private String generateQrCodeUrl(String certificateNumber) {
        return baseUrl + "/api/certificate/verify?number=" + certificateNumber;
    }
    
    /**
     * 生成证书PDF
     */
    private String generateCertificatePdf(Certificate certificate) {
        try {
            // 创建目录
            String subDir = "certificates/" + Calendar.getInstance().get(Calendar.YEAR);
            Path pdfDir = Paths.get(uploadPath, subDir);
            Files.createDirectories(pdfDir);
            
            // 生成文件名
            String filename = "cert_" + certificate.getCertificateNumber().replace("-", "_") + ".pdf";
            Path pdfPath = pdfDir.resolve(filename);
            
            // 生成PDF内容
            String pdfContent = buildCertificatePdfContent(certificate);
            
            // 写入文件
            try (FileOutputStream fos = new FileOutputStream(pdfPath.toFile())) {
                fos.write(pdfContent.getBytes(StandardCharsets.UTF_8));
            }
            
            String pdfUrl = "/uploads/" + subDir + "/" + filename;
            logger.info("证书PDF生成成功: {}", pdfUrl);
            return pdfUrl;
        } catch (IOException e) {
            logger.error("证书PDF生成失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 构建证书PDF内容
     */
    private String buildCertificatePdfContent(Certificate certificate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        
        StringBuilder content = new StringBuilder();
        content.append("%PDF-1.4\n");
        content.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        content.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        content.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 842 595] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n");
        
        // 构建证书文本内容
        StringBuilder certText = new StringBuilder();
        certText.append("BT\n");
        
        // 标题
        certText.append("/F1 36 Tf\n200 500 Td\n(CERTIFICATE) Tj\n");
        
        // 证书编号
        certText.append("/F1 12 Tf\n50 450 Td\n(Certificate No: ").append(escapeForPdf(certificate.getCertificateNumber())).append(") Tj\n");
        
        // 持证人
        certText.append("/F1 18 Tf\n50 400 Td\n(Name: ").append(escapeForPdf(certificate.getName())).append(") Tj\n");
        
        // 认证类型
        certText.append("/F1 14 Tf\n50 360 Td\n(Certification: ").append(escapeForPdf(certificate.getDescription())).append(") Tj\n");
        
        // 颁发日期
        certText.append("/F1 12 Tf\n50 320 Td\n(Issue Date: ").append(sdf.format(certificate.getIssueDate())).append(") Tj\n");
        
        // 有效期
        certText.append("/F1 12 Tf\n50 290 Td\n(Valid Until: ").append(sdf.format(certificate.getExpireDate())).append(") Tj\n");
        
        // 颁发机构
        certText.append("/F1 12 Tf\n50 250 Td\n(Issuer: ").append(escapeForPdf(certificate.getIssuer())).append(") Tj\n");
        
        // 验证URL
        certText.append("/F1 10 Tf\n50 200 Td\n(Verify at: ").append(escapeForPdf(certificate.getQrCodeUrl())).append(") Tj\n");
        
        certText.append("ET\n");
        
        String streamContent = certText.toString();
        content.append("4 0 obj\n<< /Length ").append(streamContent.length()).append(" >>\nstream\n");
        content.append(streamContent);
        content.append("endstream\nendobj\n");
        content.append("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
        content.append("xref\n0 6\n0000000000 65535 f \n");
        content.append("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n0\n%%EOF");
        
        return content.toString();
    }
    
    /**
     * 转义PDF特殊字符
     */
    private String escapeForPdf(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("(", "\\(")
                   .replace(")", "\\)")
                   .replaceAll("[^\\x00-\\x7F]", "?");
    }
    
    /**
     * 同步认证信息到简历
     */
    private void syncCertificationToResume(Long userId, Certificate certificate) {
        if (resumeService == null) return;
        
        try {
            Map<String, Object> resumeData = resumeService.getResumeByUserId(userId);
            if (resumeData == null) {
                resumeData = new HashMap<>();
            }
            
            // 获取现有证书列表
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> certificates = (List<Map<String, Object>>) resumeData.get("certificates");
            if (certificates == null) {
                certificates = new ArrayList<>();
            }
            
            // 添加新证书
            Map<String, Object> certInfo = new HashMap<>();
            certInfo.put("name", certificate.getDescription());
            certInfo.put("number", certificate.getCertificateNumber());
            certInfo.put("issueDate", new SimpleDateFormat("yyyy-MM-dd").format(certificate.getIssueDate()));
            certInfo.put("issuer", certificate.getIssuer());
            certInfo.put("type", certificate.getCertificationType());
            certInfo.put("level", certificate.getLevel());
            certificates.add(certInfo);
            
            resumeData.put("certificates", certificates);
            resumeService.saveResumeData(userId, resumeData);
            
            logger.info("认证信息已同步到简历: userId={}, certNumber={}", userId, certificate.getCertificateNumber());
        } catch (Exception e) {
            logger.error("同步认证信息到简历失败: {}", e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> checkCertificateValidity(Long certificateId) {
        Map<String, Object> result = new HashMap<>();
        
        Certificate certificate = this.getById(certificateId);
        if (certificate == null) {
            result.put("valid", false);
            result.put("message", "证书不存在");
            return result;
        }
        
        Date now = new Date();
        Date expireDate = certificate.getExpireDate();
        
        if (expireDate == null) {
            result.put("valid", true);
            result.put("message", "证书永久有效");
            result.put("remainingDays", -1);
            return result;
        }
        
        long diffInMillis = expireDate.getTime() - now.getTime();
        long remainingDays = diffInMillis / (1000 * 60 * 60 * 24);
        
        result.put("remainingDays", remainingDays);
        result.put("expireDate", expireDate);
        result.put("certificateNumber", certificate.getCertificateNumber());
        
        if (remainingDays < 0) {
            result.put("valid", false);
            result.put("expired", true);
            result.put("expiringSoon", false);
            result.put("message", "证书已过期");
            result.put("status", "expired");
        } else if (remainingDays <= 30) {
            result.put("valid", true);
            result.put("expired", false);
            result.put("expiringSoon", true);
            result.put("message", "证书即将过期，剩余" + remainingDays + "天");
            result.put("status", "expiring");
        } else if (remainingDays <= 90) {
            result.put("valid", true);
            result.put("expired", false);
            result.put("expiringSoon", true);
            result.put("message", "证书将在" + remainingDays + "天后过期");
            result.put("status", "warning");
        } else {
            result.put("valid", true);
            result.put("expired", false);
            result.put("expiringSoon", false);
            result.put("message", "证书有效");
            result.put("status", "valid");
        }
        
        return result;
    }
    
    @Override
    public List<Certificate> getExpiringCertificates(Long userId, Integer daysBeforeExpire) {
        QueryWrapper<Certificate> wrapper = new QueryWrapper<>();
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        wrapper.eq("certificate_status", "有效");
        
        // 计算过期日期范围
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, daysBeforeExpire);
        Date expireThreshold = cal.getTime();
        
        wrapper.ge("expire_date", now); // 未过期
        wrapper.le("expire_date", expireThreshold); // 在指定天数内过期
        wrapper.orderByAsc("expire_date");
        
        return certificateMapper.selectList(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean renewCertificate(Long certificateId, Integer years) {
        Certificate certificate = this.getById(certificateId);
        if (certificate == null) {
            return false;
        }
        
        // 计算新的过期日期
        Calendar cal = Calendar.getInstance();
        if (certificate.getExpireDate() != null && certificate.getExpireDate().after(new Date())) {
            // 如果证书未过期，从当前过期日期开始续期
            cal.setTime(certificate.getExpireDate());
        }
        cal.add(Calendar.YEAR, years);
        
        certificate.setExpireDate(cal.getTime());
        certificate.setCertificateStatus("有效");
        
        boolean updated = this.updateById(certificate);
        
        if (updated) {
            logger.info("证书续期成功: certificateId={}, newExpireDate={}", certificateId, cal.getTime());
        }
        
        return updated;
    }
    
    @Override
    public String generateShareLink(Long certificateId) {
        Certificate certificate = this.getById(certificateId);
        if (certificate == null) {
            return null;
        }
        
        // 生成分享码（使用证书编号的Base64编码）
        String shareCode = Base64.getUrlEncoder().encodeToString(
            (certificate.getCertificateNumber() + ":" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8)
        );
        
        return baseUrl + "/certificate/share/" + shareCode;
    }
    
    @Override
    public Map<String, Object> getCertificateByShareCode(String shareCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 解码分享码
            String decoded = new String(Base64.getUrlDecoder().decode(shareCode), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length < 1) {
                result.put("success", false);
                result.put("message", "无效的分享链接");
                return result;
            }
            
            String certificateNumber = parts[0];
            Certificate certificate = this.getByCertificateNumber(certificateNumber);
            
            if (certificate == null) {
                result.put("success", false);
                result.put("message", "证书不存在");
                return result;
            }
            
            // 返回证书公开信息
            result.put("success", true);
            result.put("certificateNumber", certificate.getCertificateNumber());
            result.put("name", certificate.getName());
            result.put("certificationType", certificate.getCertificationType());
            result.put("level", certificate.getLevel());
            result.put("description", certificate.getDescription());
            result.put("issueDate", certificate.getIssueDate());
            result.put("expireDate", certificate.getExpireDate());
            result.put("issuer", certificate.getIssuer());
            result.put("status", certificate.getCertificateStatus());
            
            // 检查有效性
            Map<String, Object> validity = checkCertificateValidity(certificate.getId());
            result.put("validity", validity);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "无效的分享链接");
        }
        
        return result;
    }
}