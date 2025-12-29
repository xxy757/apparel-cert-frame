package com.apparelcert.service.impl;

import com.apparelcert.entity.Certification;
import com.apparelcert.entity.Job;
import com.apparelcert.entity.Resume;
import com.apparelcert.mapper.JobMapper;
import com.apparelcert.service.CertificationService;
import com.apparelcert.service.JobService;
import com.apparelcert.service.ResumeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 岗位服务实现类
 */
@Service
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements JobService {

    @Autowired
    private JobMapper jobMapper;
    
    @Autowired(required = false)
    private ResumeService resumeService;
    
    @Autowired(required = false)
    private CertificationService certificationService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Page<Job> pageQuery(Integer page, Integer size, String keyword, String type, String location, String salary) {
        Page<Job> pageInfo = new Page<>(page, size);
        QueryWrapper<Job> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 只显示招聘中的岗位
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("title", keyword).or().like("description", keyword));
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        if (location != null && !location.isEmpty()) {
            wrapper.like("location", location);
        }
        if (salary != null && !salary.isEmpty()) {
            wrapper.eq("salary", salary);
        }
        
        wrapper.orderByDesc("create_time");
        return jobMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public Job getById(Long id) {
        return jobMapper.selectById(id);
    }

    @Override
    public boolean increaseViews(Long jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job != null) {
            job.setViews((job.getViews() != null ? job.getViews() : 0) + 1);
            return this.updateById(job);
        }
        return false;
    }

    @Override
    public List<Job> getRecommendedJobs(Long userId) {
        // 简单推荐：基于热门岗位
        QueryWrapper<Job> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByDesc("views");
        wrapper.last("limit 10");
        return jobMapper.selectList(wrapper);
    }
    
    @Override
    public Map<String, Object> getSmartRecommendedJobs(Long userId, Integer page, Integer size) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> recommendedJobs = new ArrayList<>();
        
        // 获取用户简历信息
        Map<String, Object> userProfile = getUserProfile(userId);
        String careerDirection = (String) userProfile.get("careerDirection");
        List<String> skills = getListFromProfile(userProfile, "skills");
        List<String> certifications = getListFromProfile(userProfile, "certifications");
        String preferredLocation = (String) userProfile.get("preferredLocation");
        
        // 查询所有招聘中的岗位
        QueryWrapper<Job> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        List<Job> allJobs = jobMapper.selectList(wrapper);
        
        // 计算每个岗位的匹配度
        for (Job job : allJobs) {
            int matchScore = calculateMatchScore(job, careerDirection, skills, certifications, preferredLocation);
            
            Map<String, Object> jobWithScore = new HashMap<>();
            jobWithScore.put("job", job);
            jobWithScore.put("matchScore", matchScore);
            jobWithScore.put("matchReasons", getMatchReasons(job, careerDirection, skills, certifications, preferredLocation));
            recommendedJobs.add(jobWithScore);
        }
        
        // 按匹配度排序
        recommendedJobs.sort((a, b) -> (Integer) b.get("matchScore") - (Integer) a.get("matchScore"));
        
        // 分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, recommendedJobs.size());
        List<Map<String, Object>> pagedJobs = start < recommendedJobs.size() ? 
            recommendedJobs.subList(start, end) : new ArrayList<>();
        
        result.put("records", pagedJobs);
        result.put("total", recommendedJobs.size());
        result.put("page", page);
        result.put("size", size);
        
        return result;
    }
    
    /**
     * 获取用户画像
     */
    private Map<String, Object> getUserProfile(Long userId) {
        Map<String, Object> profile = new HashMap<>();
        
        if (resumeService != null) {
            Map<String, Object> resumeData = resumeService.getResumeByUserId(userId);
            if (resumeData != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> basicInfo = (Map<String, Object>) resumeData.get("basicInfo");
                if (basicInfo != null) {
                    profile.put("careerDirection", basicInfo.get("careerObjective"));
                    profile.put("preferredLocation", basicInfo.get("preferredLocation"));
                }
                profile.put("skills", resumeData.get("skills"));
                profile.put("certificates", resumeData.get("certificates"));
            }
        }
        
        // 获取用户认证信息
        if (certificationService != null) {
            List<Certification> certs = certificationService.getByUserId(userId);
            List<String> certTypes = certs.stream()
                .filter(c -> c.getStatus() == 3) // 已通过的认证
                .map(Certification::getCertificationType)
                .collect(Collectors.toList());
            profile.put("certifications", certTypes);
        }
        
        return profile;
    }
    
    /**
     * 计算岗位匹配度
     */
    private int calculateMatchScore(Job job, String careerDirection, List<String> skills, 
                                    List<String> certifications, String preferredLocation) {
        int score = 0;
        
        // 职业方向匹配 (30分)
        if (careerDirection != null && job.getType() != null) {
            if (job.getType().contains(careerDirection) || careerDirection.contains(job.getType())) {
                score += 30;
            } else if (job.getTitle() != null && job.getTitle().contains(careerDirection)) {
                score += 20;
            }
        }
        
        // 技能匹配 (30分)
        if (skills != null && !skills.isEmpty() && job.getRequirements() != null) {
            int matchedSkills = 0;
            for (String skill : skills) {
                if (job.getRequirements().contains(skill) || 
                    (job.getDescription() != null && job.getDescription().contains(skill))) {
                    matchedSkills++;
                }
            }
            score += Math.min(30, matchedSkills * 10);
        }
        
        // 认证匹配 (25分)
        if (certifications != null && !certifications.isEmpty()) {
            for (String cert : certifications) {
                if (job.getType() != null && job.getType().contains(cert)) {
                    score += 25;
                    break;
                }
                if (job.getRequirements() != null && job.getRequirements().contains(cert)) {
                    score += 20;
                    break;
                }
            }
        }
        
        // 地点匹配 (15分)
        if (preferredLocation != null && job.getLocation() != null) {
            if (job.getLocation().contains(preferredLocation) || preferredLocation.contains(job.getLocation())) {
                score += 15;
            }
        }
        
        return Math.min(100, score);
    }
    
    /**
     * 获取匹配原因
     */
    private List<String> getMatchReasons(Job job, String careerDirection, List<String> skills,
                                         List<String> certifications, String preferredLocation) {
        List<String> reasons = new ArrayList<>();
        
        if (careerDirection != null && job.getType() != null && 
            (job.getType().contains(careerDirection) || careerDirection.contains(job.getType()))) {
            reasons.add("职业方向匹配");
        }
        
        if (skills != null && job.getRequirements() != null) {
            for (String skill : skills) {
                if (job.getRequirements().contains(skill)) {
                    reasons.add("技能匹配: " + skill);
                    break;
                }
            }
        }
        
        if (certifications != null) {
            for (String cert : certifications) {
                if (job.getType() != null && job.getType().contains(cert)) {
                    reasons.add("持有相关认证: " + cert);
                    break;
                }
            }
        }
        
        if (preferredLocation != null && job.getLocation() != null &&
            (job.getLocation().contains(preferredLocation) || preferredLocation.contains(job.getLocation()))) {
            reasons.add("工作地点匹配");
        }
        
        if (reasons.isEmpty()) {
            reasons.add("热门岗位推荐");
        }
        
        return reasons;
    }
    
    @SuppressWarnings("unchecked")
    private List<String> getListFromProfile(Map<String, Object> profile, String key) {
        Object value = profile.get(key);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (!list.isEmpty()) {
                if (list.get(0) instanceof String) {
                    return (List<String>) value;
                } else if (list.get(0) instanceof Map) {
                    // 从Map中提取name字段
                    return list.stream()
                        .map(item -> {
                            if (item instanceof Map) {
                                Object name = ((Map<?, ?>) item).get("name");
                                return name != null ? name.toString() : "";
                            }
                            return "";
                        })
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<Job> searchJobs(String keyword) {
        QueryWrapper<Job> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.and(w -> w.like("title", keyword).or().like("description", keyword));
        wrapper.orderByDesc("create_time");
        wrapper.last("limit 20");
        return jobMapper.selectList(wrapper);
    }

    @Override
    public Page<Job> getEnterpriseJobs(Integer page, Integer size, Long enterpriseId, Integer status) {
        Page<Job> pageInfo = new Page<>(page, size);
        QueryWrapper<Job> wrapper = new QueryWrapper<>();
        wrapper.eq("enterprise_id", enterpriseId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return jobMapper.selectPage(pageInfo, wrapper);
    }
    
    @Override
    public int batchUpdateStatus(List<Long> jobIds, Integer status) {
        int count = 0;
        for (Long jobId : jobIds) {
            Job job = new Job();
            job.setId(jobId);
            job.setStatus(status);
            if (this.updateById(job)) {
                count++;
            }
        }
        return count;
    }
    
    @Override
    public int batchDeleteJobs(List<Long> jobIds) {
        return jobMapper.deleteBatchIds(jobIds);
    }
    
    @Override
    public Map<String, Object> getJobStatistics(Long jobId) {
        Map<String, Object> stats = new HashMap<>();
        
        Job job = this.getById(jobId);
        if (job == null) {
            return stats;
        }
        
        stats.put("views", job.getViews() != null ? job.getViews() : 0);
        stats.put("applications", job.getApplications() != null ? job.getApplications() : 0);
        
        // 计算转化率
        int views = job.getViews() != null ? job.getViews() : 0;
        int applications = job.getApplications() != null ? job.getApplications() : 0;
        double conversionRate = views > 0 ? (double) applications / views * 100 : 0;
        stats.put("conversionRate", String.format("%.2f", conversionRate));
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getEnterpriseJobStatistics(Long enterpriseId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总岗位数
        QueryWrapper<Job> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("enterprise_id", enterpriseId);
        long totalCount = this.count(totalWrapper);
        stats.put("totalJobs", totalCount);
        
        // 招聘中岗位数
        QueryWrapper<Job> activeWrapper = new QueryWrapper<>();
        activeWrapper.eq("enterprise_id", enterpriseId);
        activeWrapper.eq("status", 1);
        long activeCount = this.count(activeWrapper);
        stats.put("activeJobs", activeCount);
        
        // 已关闭岗位数
        QueryWrapper<Job> closedWrapper = new QueryWrapper<>();
        closedWrapper.eq("enterprise_id", enterpriseId);
        closedWrapper.eq("status", 0);
        long closedCount = this.count(closedWrapper);
        stats.put("closedJobs", closedCount);
        
        // 总浏览量和投递量
        QueryWrapper<Job> wrapper = new QueryWrapper<>();
        wrapper.eq("enterprise_id", enterpriseId);
        List<Job> jobs = jobMapper.selectList(wrapper);
        
        int totalViews = jobs.stream().mapToInt(j -> j.getViews() != null ? j.getViews() : 0).sum();
        int totalApplications = jobs.stream().mapToInt(j -> j.getApplications() != null ? j.getApplications() : 0).sum();
        
        stats.put("totalViews", totalViews);
        stats.put("totalApplications", totalApplications);
        
        // 平均转化率
        double avgConversionRate = totalViews > 0 ? (double) totalApplications / totalViews * 100 : 0;
        stats.put("avgConversionRate", String.format("%.2f", avgConversionRate));
        
        return stats;
    }
}