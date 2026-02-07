package com.apparelcert.service.impl;

import com.apparelcert.entity.Application;
import com.apparelcert.entity.Interview;
import com.apparelcert.entity.Job;
import com.apparelcert.entity.Resume;
import com.apparelcert.entity.ResumeDelivery;
import com.apparelcert.mapper.InterviewMapper;
import com.apparelcert.mapper.JobMapper;
import com.apparelcert.mapper.ResumeDeliveryMapper;
import com.apparelcert.service.ApplicationService;
import com.apparelcert.service.InterviewService;
import com.apparelcert.service.ResumeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 面试服务实现类
 */
@Service
public class InterviewServiceImpl extends ServiceImpl<InterviewMapper, Interview> implements InterviewService {

    private static final int APPLICATION_STATUS_INTERVIEW = 2;
    private static final int APPLICATION_STATUS_REJECTED = 3;
    private static final int APPLICATION_STATUS_ACCEPTED = 4;

    @Autowired
    private InterviewMapper interviewMapper;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private ApplicationService applicationService;

    @Autowired(required = false)
    private ResumeDeliveryMapper resumeDeliveryMapper;

    @Autowired(required = false)
    private ResumeService resumeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createInterview(Interview interview) {
        if (interview == null) {
            return false;
        }

        // 兼容新链路：deliveryId 可能传入的是 application.id。
        // interview.delivery_id 受外键约束，必须转换为 resume_delivery.id。
        Application linkedApplication = null;
        Long deliveryId = interview.getDeliveryId();
        if (deliveryId != null && deliveryId > 0) {
            linkedApplication = applicationService.getById(deliveryId);
            if (linkedApplication != null) {
                if (interview.getUserId() == null) {
                    interview.setUserId(linkedApplication.getUserId());
                }
                if (interview.getJobId() == null) {
                    interview.setJobId(linkedApplication.getJobId());
                }
                if (interview.getEnterpriseId() == null) {
                    interview.setEnterpriseId(linkedApplication.getEnterpriseId());
                }
            }
        }

        Long resolvedDeliveryId = resolveResumeDeliveryId(interview, linkedApplication);
        // 兼容当前 application 链路：若无法映射到 resume_delivery，则回退使用 application.id
        if ((resolvedDeliveryId == null || resolvedDeliveryId <= 0) && linkedApplication != null && linkedApplication.getId() != null) {
            resolvedDeliveryId = linkedApplication.getId();
        }
        if ((resolvedDeliveryId == null || resolvedDeliveryId <= 0)
            && interview.getDeliveryId() != null
            && interview.getDeliveryId() > 0) {
            resolvedDeliveryId = interview.getDeliveryId();
        }
        if (resolvedDeliveryId == null || resolvedDeliveryId <= 0) {
            return false;
        }
        interview.setDeliveryId(resolvedDeliveryId);

        if (interview.getEnterpriseId() == null && interview.getJobId() != null) {
            Job job = jobMapper.selectById(interview.getJobId());
            if (job != null) {
                interview.setEnterpriseId(job.getEnterpriseId());
            }
        }

        if (interview.getStatus() == null) {
            interview.setStatus(0); // 0: 待确认
        }
        boolean saved = this.save(interview);
        if (saved) {
            syncApplicationStatusByInterview(interview, APPLICATION_STATUS_INTERVIEW);
        }
        return saved;
    }

    @Override
    public Page<Interview> getEnterpriseInterviews(Integer page, Integer size, Long enterpriseId, Integer status) {
        Page<Interview> pageInfo = new Page<>(page, size);
        QueryWrapper<Interview> wrapper = new QueryWrapper<>();
        wrapper.eq("enterprise_id", enterpriseId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("interview_time");
        return interviewMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public Page<Interview> getUserInterviews(Integer page, Integer size, Long userId, Integer status) {
        Page<Interview> pageInfo = new Page<>(page, size);
        QueryWrapper<Interview> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("interview_time");
        return interviewMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public boolean updateInterviewStatus(Long interviewId, Integer status) {
        Interview interview = new Interview();
        interview.setId(interviewId);
        interview.setStatus(status);
        return this.updateById(interview);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateInterviewResult(Long interviewId, Integer result, String feedback) {
        Interview interview = this.getById(interviewId);
        if (interview == null) {
            return false;
        }

        interview.setResult(result);
        interview.setFeedback(feedback);
        boolean updated = this.updateById(interview);
        if (!updated || result == null) {
            return updated;
        }

        if (result == 1) {
            syncApplicationStatusByInterview(interview, APPLICATION_STATUS_ACCEPTED);
        } else if (result == 0 || result == 2) {
            syncApplicationStatusByInterview(interview, APPLICATION_STATUS_REJECTED);
        }
        return true;
    }
    
    @Override
    public boolean recordInterviewFeedback(Long interviewId, String feedback, Integer evaluation, String strengths, String weaknesses) {
        Interview interview = this.getById(interviewId);
        if (interview == null) {
            return false;
        }
        
        interview.setFeedback(feedback);
        interview.setEvaluation(evaluation);
        interview.setStrengths(strengths);
        interview.setWeaknesses(weaknesses);
        interview.setStatus(2); // 已面试
        
        return this.updateById(interview);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateHireStatus(Long interviewId, Integer hireStatus, String salary, String entryDate) {
        Interview interview = this.getById(interviewId);
        if (interview == null) {
            return false;
        }
        
        interview.setHireStatus(hireStatus);
        if (hireStatus == 1) { // 录用
            interview.setSalary(salary);
            interview.setEntryDate(entryDate);
            interview.setResult(1); // 通过
        } else if (hireStatus == 2) { // 不录用
            interview.setResult(0); // 未通过
        }

        boolean updated = this.updateById(interview);
        if (!updated) {
            return false;
        }

        if (hireStatus == 1) {
            syncApplicationStatusByInterview(interview, APPLICATION_STATUS_ACCEPTED);
        } else if (hireStatus == 2) {
            syncApplicationStatusByInterview(interview, APPLICATION_STATUS_REJECTED);
        }
        return true;
    }
    
    @Override
    public Map<String, Object> getInterviewDetail(Long interviewId) {
        Map<String, Object> result = new HashMap<>();
        
        Interview interview = this.getById(interviewId);
        if (interview == null) {
            result.put("success", false);
            result.put("message", "面试记录不存在");
            return result;
        }
        
        result.put("success", true);
        result.put("interview", interview);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getInterviewStatistics(Long enterpriseId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总面试数
        QueryWrapper<Interview> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("enterprise_id", enterpriseId);
        long totalCount = this.count(totalWrapper);
        stats.put("totalCount", totalCount);
        
        // 待面试数
        QueryWrapper<Interview> pendingWrapper = new QueryWrapper<>();
        pendingWrapper.eq("enterprise_id", enterpriseId);
        pendingWrapper.in("status", 0, 1);
        long pendingCount = this.count(pendingWrapper);
        stats.put("pendingCount", pendingCount);
        
        // 已面试数
        QueryWrapper<Interview> completedWrapper = new QueryWrapper<>();
        completedWrapper.eq("enterprise_id", enterpriseId);
        completedWrapper.eq("status", 2);
        long completedCount = this.count(completedWrapper);
        stats.put("completedCount", completedCount);
        
        // 录用数
        QueryWrapper<Interview> hiredWrapper = new QueryWrapper<>();
        hiredWrapper.eq("enterprise_id", enterpriseId);
        hiredWrapper.eq("hire_status", 1);
        long hiredCount = this.count(hiredWrapper);
        stats.put("hiredCount", hiredCount);
        
        // 本月面试数
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date monthStart = cal.getTime();
        
        QueryWrapper<Interview> monthWrapper = new QueryWrapper<>();
        monthWrapper.eq("enterprise_id", enterpriseId);
        monthWrapper.ge("interview_time", monthStart);
        long monthCount = this.count(monthWrapper);
        stats.put("monthCount", monthCount);
        
        // 录用率
        double hireRate = completedCount > 0 ? (double) hiredCount / completedCount * 100 : 0;
        stats.put("hireRate", String.format("%.1f", hireRate));
        
        return stats;
    }

    private void syncApplicationStatusByInterview(Interview interview, Integer status) {
        if (interview == null || status == null) {
            return;
        }

        Application application = null;

        // 优先按投递记录ID关联（deliveryId 在当前业务中即 application.id）
        if (interview.getDeliveryId() != null) {
            application = applicationService.getById(interview.getDeliveryId());
            // interview.delivery_id 可能是 resume_delivery.id，若与当前面试用户/岗位不匹配则忽略
            if (application != null) {
                if ((interview.getUserId() != null && !interview.getUserId().equals(application.getUserId()))
                    || (interview.getJobId() != null && !interview.getJobId().equals(application.getJobId()))) {
                    application = null;
                }
            }
        }

        // 兼容无 deliveryId 的历史/其他入口，回退按 jobId + userId 关联
        if (application == null && interview.getJobId() != null && interview.getUserId() != null) {
            QueryWrapper<Application> wrapper = new QueryWrapper<>();
            wrapper.eq("job_id", interview.getJobId());
            wrapper.eq("user_id", interview.getUserId());
            wrapper.orderByDesc("apply_time");
            wrapper.last("limit 1");
            application = applicationService.getOne(wrapper, false);
        }

        if (application == null || status.equals(application.getStatus())) {
            return;
        }

        Application update = new Application();
        update.setId(application.getId());
        update.setStatus(status);
        update.setFeedbackTime(new Date());
        applicationService.updateById(update);
    }

    private Long resolveResumeDeliveryId(Interview interview, Application linkedApplication) {
        if (interview == null || resumeDeliveryMapper == null) {
            return null;
        }

        // 1) 已经是有效的 resume_delivery.id 直接使用
        Long rawDeliveryId = interview.getDeliveryId();
        if (rawDeliveryId != null && rawDeliveryId > 0 && existsInResumeDelivery(rawDeliveryId)) {
            return rawDeliveryId;
        }

        Long userId = interview.getUserId();
        Long positionId = interview.getJobId();
        if (linkedApplication != null) {
            if (userId == null) {
                userId = linkedApplication.getUserId();
            }
            if (positionId == null) {
                positionId = linkedApplication.getJobId();
            }
        }
        if (userId == null || positionId == null) {
            return null;
        }

        // 2) 按 user + position 找已有投递
        QueryWrapper<ResumeDelivery> query = new QueryWrapper<>();
        query.eq("user_id", userId);
        query.eq("position_id", positionId);
        query.orderByDesc("create_time");
        query.last("limit 1");
        ResumeDelivery existing = resumeDeliveryMapper.selectOne(query);
        if (existing != null && existing.getId() != null) {
            return existing.getId();
        }

        // 3) 按 user 兜底找最近投递，兼容 positionId 体系不一致场景
        QueryWrapper<ResumeDelivery> userOnlyQuery = new QueryWrapper<>();
        userOnlyQuery.eq("user_id", userId);
        userOnlyQuery.orderByDesc("create_time");
        userOnlyQuery.last("limit 1");
        ResumeDelivery latestByUser = resumeDeliveryMapper.selectOne(userOnlyQuery);
        if (latestByUser != null && latestByUser.getId() != null) {
            return latestByUser.getId();
        }

        // 4) 仍不存在则自动补一条 resume_delivery，满足 interview 外键约束
        if (resumeService == null) {
            return null;
        }
        Resume resume = resumeService.getByUserId(userId);
        if (resume == null || resume.getId() == null) {
            return null;
        }

        ResumeDelivery shadowDelivery = new ResumeDelivery();
        shadowDelivery.setUserId(userId);
        shadowDelivery.setPositionId(positionId);
        shadowDelivery.setResumeId(resume.getId());
        shadowDelivery.setStatus(linkedApplication != null && linkedApplication.getStatus() != null
            ? linkedApplication.getStatus() : 0);

        try {
            int inserted = resumeDeliveryMapper.insert(shadowDelivery);
            if (inserted > 0) {
                return shadowDelivery.getId();
            }
        } catch (Exception ignored) {
            // 可能因 position_id/resume_id 外键约束导致插入失败，返回 null 由上层处理
        }
        return null;
    }

    private boolean existsInResumeDelivery(Long deliveryId) {
        return deliveryId != null
            && deliveryId > 0
            && resumeDeliveryMapper != null
            && resumeDeliveryMapper.selectById(deliveryId) != null;
    }
}
