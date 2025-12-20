package com.apparelcert.service.impl;

import com.apparelcert.entity.ResumeDelivery;
import com.apparelcert.mapper.ResumeDeliveryMapper;
import com.apparelcert.service.ResumeDeliveryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 简历投递服务实现类
 */
@Service
public class ResumeDeliveryServiceImpl extends ServiceImpl<ResumeDeliveryMapper, ResumeDelivery> implements ResumeDeliveryService {

    @Override
    public List<ResumeDelivery> getEnterpriseResumes(Long enterpriseId, Map<String, Object> params) {
        // TODO: 根据企业ID获取收到的简历列表
        return list();
    }

    @Override
    public ResumeDelivery getDeliveryDetail(Long id) {
        return getById(id);
    }

    @Override
    public boolean markAsViewed(Long id) {
        ResumeDelivery delivery = getById(id);
        if (delivery != null) {
            delivery.setStatus(1); // 1表示已查看
            return updateById(delivery);
        }
        return false;
    }

    @Override
    public boolean sendInterviewInvitation(Long deliveryId, Map<String, Object> interviewInfo) {
        ResumeDelivery delivery = getById(deliveryId);
        if (delivery != null) {
            delivery.setStatus(2); // 2表示面试邀请
            boolean updated = updateById(delivery);
            // TODO: 创建面试记录
            return updated;
        }
        return false;
    }

    @Override
    public boolean rejectResume(Long id) {
        ResumeDelivery delivery = getById(id);
        if (delivery != null) {
            delivery.setStatus(3); // 3表示已拒绝
            return updateById(delivery);
        }
        return false;
    }
}
