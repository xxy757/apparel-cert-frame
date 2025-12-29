package com.apparelcert.service;

import com.apparelcert.entity.Certification;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 技能认证服务接口
 */
public interface CertificationService extends IService<Certification> {
    /**
     * 申请技能认证
     */
    boolean applyCertification(Certification certification);
    
    /**
     * 获取用户认证列表
     */
    List<Certification> getByUserId(Long userId);
    
    /**
     * 获取认证详情
     */
    Certification getById(Long id);
    
    /**
     * 上传实操成果
     */
    boolean uploadPractical(Long certificationId, String fileUrl);
    
    /**
     * 获取认证进度
     */
    Integer getProgress(Long certificationId);
    
    /**
     * 分页查询认证记录
     */
    Page<Certification> pageQuery(Integer page, Integer size, Long userId);
    
    /**
     * 更新认证状态
     */
    boolean updateStatus(Long certificationId, Integer status, String reviewComment);
    
    /**
     * 认证通过后同步信息到简历
     * @param certificationId 认证ID
     * @return 是否成功
     */
    boolean syncToResume(Long certificationId);
    
    /**
     * 完成认证（通过后自动生成证书并同步简历）
     * @param certificationId 认证ID
     * @param theoryScore 理论成绩
     * @param practicalScore 实操成绩
     * @param reviewComment 评审意见
     * @return 是否成功
     */
    boolean completeCertification(Long certificationId, String theoryScore, String practicalScore, String reviewComment);
    
    /**
     * 更新理论考试成绩
     * @param applicationId 认证申请ID
     * @param score 理论成绩
     * @return 是否成功
     */
    boolean updateTheoryScore(Long applicationId, int score);
}