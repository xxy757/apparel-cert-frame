package com.apparelcert.service;

import com.apparelcert.entity.ResumeDelivery;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 简历投递服务接口
 */
public interface ResumeDeliveryService extends IService<ResumeDelivery> {

    /**
     * 获取企业收到的简历列表
     */
    List<ResumeDelivery> getEnterpriseResumes(Long enterpriseId, Map<String, Object> params);

    /**
     * 获取简历投递详情
     */
    ResumeDelivery getDeliveryDetail(Long id);

    /**
     * 标记简历为已查看
     */
    boolean markAsViewed(Long id);

    /**
     * 发送面试邀请
     */
    boolean sendInterviewInvitation(Long deliveryId, Map<String, Object> interviewInfo);

    /**
     * 拒绝简历
     */
    boolean rejectResume(Long id);
}
