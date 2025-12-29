package com.apparelcert.service.impl;

import com.apparelcert.entity.Certification;
import com.apparelcert.entity.ExpertReview;
import com.apparelcert.mapper.ExpertReviewMapper;
import com.apparelcert.service.CertificationService;
import com.apparelcert.service.EmailService;
import com.apparelcert.service.ExpertReviewService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 专家评审服务实现类
 */
@Service
public class ExpertReviewServiceImpl extends ServiceImpl<ExpertReviewMapper, ExpertReview> implements ExpertReviewService {

    @Autowired
    private ExpertReviewMapper expertReviewMapper;

    @Autowired
    private CertificationService certificationService;

    @Autowired
    private EmailService emailService;

    @Override
    public Page<ExpertReview> getPendingReviews(Long expertId, Integer page, Integer size, Integer reviewType) {
        Page<ExpertReview> pageInfo = new Page<>(page, size);
        QueryWrapper<ExpertReview> wrapper = new QueryWrapper<>();
        wrapper.eq("expert_id", expertId);
        wrapper.eq("status", 0); // 待评审
        if (reviewType != null) {
            wrapper.eq("review_type", reviewType);
        }
        wrapper.orderByAsc("assign_time");
        return expertReviewMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public Page<ExpertReview> getCompletedReviews(Long expertId, Integer page, Integer size) {
        Page<ExpertReview> pageInfo = new Page<>(page, size);
        QueryWrapper<ExpertReview> wrapper = new QueryWrapper<>();
        wrapper.eq("expert_id", expertId);
        wrapper.in("status", 1, 2); // 已评审或已驳回
        wrapper.orderByDesc("review_time");
        return expertReviewMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public Map<String, Object> getReviewDetail(Long reviewId) {
        Map<String, Object> result = new HashMap<>();
        
        ExpertReview review = this.getById(reviewId);
        if (review == null) {
            result.put("success", false);
            result.put("message", "评审记录不存在");
            return result;
        }
        
        result.put("success", true);
        result.put("review", review);
        
        // 获取关联的认证信息
        Certification certification = certificationService.getById(review.getCertificationId());
        if (certification != null) {
            result.put("certification", certification);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitReview(Long reviewId, Integer score, String grade, String comment, Boolean passed) {
        ExpertReview review = this.getById(reviewId);
        if (review == null || review.getStatus() != 0) {
            return false;
        }
        
        // 更新评审记录
        review.setScore(score);
        review.setGrade(grade);
        review.setComment(comment);
        review.setStatus(1); // 已评审
        review.setReviewTime(new Date());
        
        boolean updated = this.updateById(review);
        
        if (updated) {
            // 更新认证状态
            Certification certification = certificationService.getById(review.getCertificationId());
            if (certification != null) {
                if (review.getReviewType() == 1) {
                    // 理论考试评审
                    certification.setTheoryScore(String.valueOf(score));
                } else if (review.getReviewType() == 2) {
                    // 实操评审
                    certification.setPracticalScore(String.valueOf(score));
                }
                
                // 如果通过，更新认证状态
                if (passed != null && passed) {
                    certification.setStatus(3); // 已通过
                    certification.setReviewComment(comment);
                    certification.setReviewTime(new Date());
                    certification.setReviewerId(review.getExpertId());
                }
                
                certificationService.updateById(certification);
            }
        }
        
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectReview(Long reviewId, String reason) {
        ExpertReview review = this.getById(reviewId);
        if (review == null || review.getStatus() != 0) {
            return false;
        }
        
        review.setStatus(2); // 已驳回
        review.setComment(reason);
        review.setReviewTime(new Date());
        
        boolean updated = this.updateById(review);
        
        if (updated) {
            // 更新认证状态为未通过
            Certification certification = certificationService.getById(review.getCertificationId());
            if (certification != null) {
                certification.setStatus(4); // 未通过
                certification.setReviewComment(reason);
                certification.setReviewTime(new Date());
                certification.setReviewerId(review.getExpertId());
                certificationService.updateById(certification);
            }
        }
        
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignReview(Long certificationId, Long expertId, Integer reviewType) {
        // 检查是否已分配
        QueryWrapper<ExpertReview> wrapper = new QueryWrapper<>();
        wrapper.eq("certification_id", certificationId);
        wrapper.eq("review_type", reviewType);
        wrapper.eq("status", 0);
        
        ExpertReview existing = expertReviewMapper.selectOne(wrapper);
        if (existing != null) {
            return false; // 已有待评审任务
        }
        
        // 创建评审任务
        ExpertReview review = new ExpertReview();
        review.setCertificationId(certificationId);
        review.setExpertId(expertId);
        review.setReviewType(reviewType);
        review.setStatus(0); // 待评审
        review.setAssignTime(new Date());
        
        return this.save(review);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAssignReviews(List<Long> certificationIds, Long expertId, Integer reviewType) {
        int count = 0;
        for (Long certificationId : certificationIds) {
            if (assignReview(certificationId, expertId, reviewType)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Map<String, Object> getExpertStatistics(Long expertId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 待评审数量
        QueryWrapper<ExpertReview> pendingWrapper = new QueryWrapper<>();
        pendingWrapper.eq("expert_id", expertId);
        pendingWrapper.eq("status", 0);
        long pendingCount = this.count(pendingWrapper);
        stats.put("pendingCount", pendingCount);
        
        // 已评审数量
        QueryWrapper<ExpertReview> completedWrapper = new QueryWrapper<>();
        completedWrapper.eq("expert_id", expertId);
        completedWrapper.eq("status", 1);
        long completedCount = this.count(completedWrapper);
        stats.put("completedCount", completedCount);
        
        // 已驳回数量
        QueryWrapper<ExpertReview> rejectedWrapper = new QueryWrapper<>();
        rejectedWrapper.eq("expert_id", expertId);
        rejectedWrapper.eq("status", 2);
        long rejectedCount = this.count(rejectedWrapper);
        stats.put("rejectedCount", rejectedCount);
        
        // 总数量
        stats.put("totalCount", pendingCount + completedCount + rejectedCount);
        
        // 本月评审数量
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date monthStart = cal.getTime();
        
        QueryWrapper<ExpertReview> monthWrapper = new QueryWrapper<>();
        monthWrapper.eq("expert_id", expertId);
        monthWrapper.ge("review_time", monthStart);
        monthWrapper.in("status", 1, 2);
        long monthCount = this.count(monthWrapper);
        stats.put("monthCount", monthCount);
        
        return stats;
    }

    @Override
    public List<ExpertReview> getReviewsByCertification(Long certificationId) {
        QueryWrapper<ExpertReview> wrapper = new QueryWrapper<>();
        wrapper.eq("certification_id", certificationId);
        wrapper.orderByDesc("review_time");
        return expertReviewMapper.selectList(wrapper);
    }
}
