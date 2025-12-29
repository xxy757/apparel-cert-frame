package com.apparelcert.service;

import com.apparelcert.entity.ResumeCollection;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 简历收藏服务接口
 */
public interface ResumeCollectionService extends IService<ResumeCollection> {

    /**
     * 收藏简历
     * @param enterpriseId 企业ID
     * @param resumeUserId 简历用户ID
     * @return 是否成功
     */
    boolean collectResume(Long enterpriseId, Long resumeUserId);

    /**
     * 取消收藏
     * @param enterpriseId 企业ID
     * @param resumeUserId 简历用户ID
     * @return 是否成功
     */
    boolean uncollectResume(Long enterpriseId, Long resumeUserId);

    /**
     * 检查是否已收藏
     * @param enterpriseId 企业ID
     * @param resumeUserId 简历用户ID
     * @return 是否已收藏
     */
    boolean isCollected(Long enterpriseId, Long resumeUserId);

    /**
     * 更新意向程度
     * @param enterpriseId 企业ID
     * @param resumeUserId 简历用户ID
     * @param intentionLevel 意向程度
     * @return 是否成功
     */
    boolean updateIntentionLevel(Long enterpriseId, Long resumeUserId, Integer intentionLevel);

    /**
     * 更新备注
     * @param enterpriseId 企业ID
     * @param resumeUserId 简历用户ID
     * @param notes 备注
     * @return 是否成功
     */
    boolean updateNotes(Long enterpriseId, Long resumeUserId, String notes);

    /**
     * 更新标签
     * @param enterpriseId 企业ID
     * @param resumeUserId 简历用户ID
     * @param tags 标签
     * @return 是否成功
     */
    boolean updateTags(Long enterpriseId, Long resumeUserId, String tags);

    /**
     * 获取企业收藏的简历列表
     * @param enterpriseId 企业ID
     * @param page 页码
     * @param size 每页数量
     * @param intentionLevel 意向程度筛选
     * @return 分页结果
     */
    Page<ResumeCollection> getCollectedResumes(Long enterpriseId, Integer page, Integer size, Integer intentionLevel);

    /**
     * 获取收藏详情
     * @param enterpriseId 企业ID
     * @param resumeUserId 简历用户ID
     * @return 收藏详情
     */
    ResumeCollection getCollectionDetail(Long enterpriseId, Long resumeUserId);

    /**
     * 批量收藏
     * @param enterpriseId 企业ID
     * @param resumeUserIds 简历用户ID列表
     * @return 成功收藏数量
     */
    int batchCollect(Long enterpriseId, List<Long> resumeUserIds);

    /**
     * 批量取消收藏
     * @param enterpriseId 企业ID
     * @param resumeUserIds 简历用户ID列表
     * @return 成功取消数量
     */
    int batchUncollect(Long enterpriseId, List<Long> resumeUserIds);
}
