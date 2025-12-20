package com.apparelcert.service.impl;

import com.apparelcert.entity.Certificate;
import com.apparelcert.entity.Certification;
import com.apparelcert.mapper.CertificateMapper;
import com.apparelcert.service.CertificateService;
import com.apparelcert.service.CertificationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 证书服务实现类
 */
@Service
public class CertificateServiceImpl extends ServiceImpl<CertificateMapper, Certificate> implements CertificateService {

    @Autowired
    private CertificateMapper certificateMapper;
    
    @Autowired
    private CertificationService certificationService;

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
            wrapper.eq("status", status);
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
    public Certificate generateCertificate(Long certificationId) {
        // 获取认证信息
        Certification certification = certificationService.getById(certificationId);
        if (certification == null) {
            return null;
        }
        
        // 生成证书
        Certificate certificate = new Certificate();
        certificate.setUserId(certification.getUserId());
        certificate.setCertificationType(certification.getCertificationType());
        certificate.setLevel(certification.getLevel());
        certificate.setCertificateNumber(generateCertificateNumber());
        certificate.setName("张三"); // TODO: 从用户信息中获取
        certificate.setIdCard("123456789012345678"); // TODO: 从用户信息中获取
        certificate.setIssueDate(new Date());
        certificate.setExpireDate(new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)); // 有效期1年
        certificate.setStatus("有效");
        certificate.setIssuer("服装行业人才技能认证中心");
        certificate.setDescription("服装行业技能认证证书");
        
        this.save(certificate);
        return certificate;
    }

    @Override
    public boolean verifyCertificate(String certificateNumber) {
        Certificate certificate = this.getByCertificateNumber(certificateNumber);
        return certificate != null && "有效".equals(certificate.getStatus()) && certificate.getExpireDate().after(new Date());
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
        certificate.setStatus(status);
        return this.updateById(certificate);
    }

    @Override
    public String exportCertificateToPdf(Long certificateId) {
        // TODO: 实现导出证书为PDF的功能
        return "/pdf/certificate_" + certificateId + ".pdf";
    }
    
    /**
     * 生成唯一证书编号
     */
    private String generateCertificateNumber() {
        return "AC" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}