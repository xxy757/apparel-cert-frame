package com.apparelcert.service;

import com.apparelcert.entity.ExpertReview;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 专家评审服务接口
 */
public interface ExpertReviewService extends IService<ExpertReview> {

    /**
     * 获取待评审列表
     * @param expertId 专家ID
     * @param page 页码
     * @param size 每页数量
     * @param reviewType 评审类型
     * @return 分页结果
     */
    Page<ExpertReview> getPendingReviews(Long expertId, Integer page, Integer size, Integer reviewType);

    /**
     * 获取已评审列表
     * @param expertId 专家ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    Page<ExpertReview> getCompletedReviews(Long expertId, Integer page, Integer size);

    /**
     * 获取评审详情
     * @param reviewId 评审ID
     * @return 评审详情（包含认证信息）
     */
    Map<String, Object> getReviewDetail(Long reviewId);

    /**
     * 提交评审结果
     * @param reviewId 评审ID
     * @param score 分数
     * @param grade 等级
     * @param comment 评审意见
     * @param passed 是否通过
     * @return 是否成功
     */
    boolean submitReview(Long reviewId, Integer score, String grade, String comment, Boolean passed);

    /**
     * 驳回评审
     * @param reviewId 评审ID
     * @param reason 驳回原因
     * @return 是否成功
     */
    boolean rejectReview(Long reviewId, String reason);

    /**
     * 分配评审任务
     * @param certificationId 认证ID
     * @param expertId 专家ID
     * @param reviewType 评审类型
     * @return 是否成功
     */
    boolean assignReview(Long certificationId, Long expertId, Integer reviewType);

    /**
     * 批量分配评审任务
     * @param certificationIds 认证ID列表
     * @param expertId 专家ID
     * @param reviewType 评审类型
     * @return 成功分配数量
     */
    int batchAssignReviews(List<Long> certificationIds, Long expertId, Integer reviewType);

    /**
     * 获取专家评审统计
     * @param expertId 专家ID
     * @return 统计信息
     */
    Map<String, Object> getExpertStatistics(Long expertId);

    /**
     * 获取认证的所有评审记录
     * @param certificationId 认证ID
     * @return 评审记录列表
     */
    List<ExpertReview> getReviewsByCertification(Long certificationId);
}
