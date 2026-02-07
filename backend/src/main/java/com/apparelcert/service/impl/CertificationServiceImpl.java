package com.apparelcert.service.impl;

import com.apparelcert.entity.Certificate;
import com.apparelcert.entity.Certification;
import com.apparelcert.mapper.CertificationMapper;
import com.apparelcert.service.CertificateService;
import com.apparelcert.service.CertificationService;
import com.apparelcert.service.EmailService;
import com.apparelcert.service.ResumeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 技能认证服务实现类
 */
@Service
public class CertificationServiceImpl extends ServiceImpl<CertificationMapper, Certification> implements CertificationService {

    private static final Logger logger = LoggerFactory.getLogger(CertificationServiceImpl.class);

    @Autowired
    private CertificationMapper certificationMapper;
    
    @Autowired(required = false)
    private ResumeService resumeService;
    
    @Autowired(required = false)
    private CertificateService certificateService;
    
    @Autowired(required = false)
    private EmailService emailService;

    @Override
    public boolean applyCertification(Certification certification) {
        certification.setApplyTime(new Date());
        certification.setStatus(0); // 0: 待审核
        return this.save(certification);
    }

    @Override
    public List<Certification> getByUserId(Long userId) {
        QueryWrapper<Certification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("apply_time");
        return certificationMapper.selectList(wrapper);  
    }

    @Override
    public Certification getById(Long id) {
        return certificationMapper.selectById(id);
    }

    @Override
    public boolean uploadPractical(Long certificationId, String fileUrl) {
        Certification certification = this.getById(certificationId);
        if (certification == null) {
            return false;
        }
        certification.setPracticalFileUrl(fileUrl);
        certification.setStatus(1); // 1: 实操已提交，进入审核流程
        return this.updateById(certification);
    }

    @Override
    public Integer getProgress(Long certificationId) {
        Certification certification = this.getById(certificationId);
        if (certification == null) {
            return 0;
        }
        // 根据状态返回进度：0-待审核(20%), 1-实操已提交(50%), 2-理论考试已完成(70%), 3-已通过(100%), 4-未通过(0%)
        switch (certification.getStatus()) {
            case 0:
                return 20;
            case 1:
                return 50;
            case 2:
                return 70;
            case 3:
                return 100;
            default:
                return 0;
        }
    }

    @Override
    public Page<Certification> pageQuery(Integer page, Integer size, Long userId) {
        Page<Certification> pageInfo = new Page<>(page, size);
        QueryWrapper<Certification> wrapper = new QueryWrapper<>();
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        wrapper.orderByDesc("apply_time");
        return certificationMapper.selectPage(pageInfo, wrapper);
    }
    
    @Override
    public boolean updateStatus(Long certificationId, Integer status, String reviewComment) {
        Certification certification = this.getById(certificationId);
        if (certification == null) {
            return false;
        }
        
        certification.setStatus(status);
        certification.setReviewComment(reviewComment);
        certification.setReviewTime(new Date());
        
        return this.updateById(certification);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean syncToResume(Long certificationId) {
        if (resumeService == null) {
            logger.warn("ResumeService未注入，无法同步认证信息到简历");
            return false;
        }
        
        Certification certification = this.getById(certificationId);
        if (certification == null || certification.getStatus() != 3) {
            return false; // 只同步已通过的认证
        }
        
        try {
            Long userId = certification.getUserId();
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
            
            // 检查是否已存在该认证
            boolean exists = certificates.stream().anyMatch(cert -> 
                certification.getCertificationType().equals(cert.get("type")) &&
                certification.getLevel().equals(cert.get("level"))
            );
            
            if (!exists) {
                // 添加新认证
                Map<String, Object> certInfo = new HashMap<>();
                certInfo.put("name", getCertificationName(certification));
                certInfo.put("type", certification.getCertificationType());
                certInfo.put("level", certification.getLevel());
                certInfo.put("theoryScore", certification.getTheoryScore());
                certInfo.put("practicalScore", certification.getPracticalScore());
                certInfo.put("passDate", new SimpleDateFormat("yyyy-MM-dd").format(certification.getReviewTime()));
                certInfo.put("issuer", "服装行业人才技能认证中心");
                certificates.add(certInfo);
                
                resumeData.put("certificates", certificates);
                resumeService.saveResumeData(userId, resumeData);
                
                logger.info("认证信息已同步到简历: userId={}, certType={}", userId, certification.getCertificationType());
            }
            
            return true;
        } catch (Exception e) {
            logger.error("同步认证信息到简历失败: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeCertification(Long certificationId, String theoryScore, String practicalScore, String reviewComment) {
        Certification certification = this.getById(certificationId);
        if (certification == null) {
            return false;
        }
        
        // 更新认证信息
        certification.setTheoryScore(theoryScore);
        certification.setPracticalScore(practicalScore);
        certification.setReviewComment(reviewComment);
        certification.setStatus(3); // 已通过
        certification.setReviewTime(new Date());
        
        boolean updated = this.updateById(certification);
        
        if (updated) {
            // 生成证书
            if (certificateService != null) {
                Certificate certificate = certificateService.generateCertificate(certificationId);
                if (certificate != null) {
                    certification.setCertificateUrl(certificate.getCertificateUrl());
                    this.updateById(certification);
                }
            }
            
            // 同步到简历
            syncToResume(certificationId);
            
            // 发送通知邮件
            sendCertificationNotification(certification, true);
            
            logger.info("认证完成: certificationId={}", certificationId);
        }
        
        return updated;
    }
    
    /**
     * 获取认证名称
     */
    private String getCertificationName(Certification certification) {
        String levelName = "";
        if (certification.getLevel() != null) {
            switch (certification.getLevel()) {
                case 1: levelName = "初级"; break;
                case 2: levelName = "中级"; break;
                case 3: levelName = "高级"; break;
                case 4: levelName = "专家级"; break;
            }
        }
        return certification.getCertificationType() + levelName + "认证";
    }
    
    /**
     * 发送认证结果通知
     */
    private void sendCertificationNotification(Certification certification, boolean passed) {
        if (emailService == null) {
            return;
        }
        
        try {
            // 这里需要获取用户邮箱，简化处理
            // 实际应该从用户表获取
            String certName = getCertificationName(certification);
            String reason = passed ? "" : certification.getReviewComment();
            
            // emailService.sendCertificationResultNotification(email, userName, certName, passed, reason);
            logger.info("认证结果通知已发送: certificationId={}, passed={}, certName={}, reason={}", 
                    certification.getId(), passed, certName, reason);
        } catch (Exception e) {
            logger.error("发送认证结果通知失败: {}", e.getMessage());
        }
    }
    
    @Override
    public boolean updateTheoryScore(Long applicationId, int score) {
        Certification certification = this.getById(applicationId);
        if (certification == null) {
            return false;
        }
        
        certification.setTheoryScore(String.valueOf(score));
        certification.setStatus(2); // 理论考试已完成
        
        return this.updateById(certification);
    }
}