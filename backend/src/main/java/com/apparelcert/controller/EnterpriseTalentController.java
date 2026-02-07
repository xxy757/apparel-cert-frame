package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Application;
import com.apparelcert.entity.Interview;
import com.apparelcert.entity.Job;
import com.apparelcert.entity.Resume;
import com.apparelcert.entity.UserPersonal;
import com.apparelcert.service.ApplicationService;
import com.apparelcert.service.InterviewService;
import com.apparelcert.service.JobService;
import com.apparelcert.service.ResumeService;
import com.apparelcert.service.UserPersonalService;
import com.apparelcert.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 企业用户人才筛选与面试管理控制器
 */
@RestController
@RequestMapping("/api/enterprise/talent")
public class EnterpriseTalentController {

    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private InterviewService interviewService;
    
    @Autowired
    private ResumeService resumeService;

    @Autowired
    private JobService jobService;

    @Autowired
    private UserPersonalService userPersonalService;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取岗位投递记录
     */
    @GetMapping("/applications")
    public Result<Page<Map<String, Object>>> getApplications(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Long enterpriseId,
            @RequestParam(required = false) Integer status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long resolvedEnterpriseId = enterpriseId;
        if (jobId == null && resolvedEnterpriseId == null && authHeader != null && !authHeader.isEmpty()) {
            try {
                String token = authHeader.replace("Bearer ", "");
                Integer userType = jwtUtil.getUserTypeFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);
                if (userType != null && userType == 2) {
                    resolvedEnterpriseId = userId;
                }
            } catch (Exception ignored) {
                // ignore token parse errors and let validation handle it
            }
        }

        Page<Application> pageInfo;
        if (jobId != null) {
            pageInfo = applicationService.pageQuery(page, size, null, jobId, status);
        } else if (resolvedEnterpriseId != null) {
            pageInfo = applicationService.getEnterpriseApplications(page, size, resolvedEnterpriseId, status);
        } else {
            return Result.error(400, "jobId 或 enterpriseId 必填");
        }

        Page<Map<String, Object>> resultPage =
                new Page<>(pageInfo.getCurrent(), pageInfo.getSize(), pageInfo.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        for (Application application : pageInfo.getRecords()) {
            records.add(buildApplicationView(application));
        }
        resultPage.setRecords(records);
        return Result.success(resultPage);
    }

    /**
     * 更新投递状态
     */
    @PutMapping("/application/status")
    public Result<Boolean> updateApplicationStatus(
            @RequestParam Long applicationId,
            @RequestParam Integer status) {
        boolean result = applicationService.updateStatus(applicationId, status);
        return Result.success(result);
    }

    /**
     * 获取简历详情
     */
    @GetMapping("/resume")
    public Result<Resume> getResumeDetail(@RequestParam Long resumeId) {
        Resume resume = resumeService.getById(resumeId);
        return Result.success(resume);
    }

    /**
     * 根据用户ID获取简历详情（结构化数据）
     */
    @GetMapping("/resume/user")
    public Result<Map<String, Object>> getResumeByUserId(@RequestParam Long userId) {
        Map<String, Object> resumeData = resumeService.getResumeByUserId(userId);
        if (resumeData == null || resumeData.isEmpty()) {
            return Result.error(404, "简历不存在");
        }
        return Result.success(resumeData);
    }

    /**
     * 发送面试邀请
     */
    @PostMapping("/interview")
    public Result<Boolean> sendInterviewInvitation(@RequestBody Interview interview) {
        if (interview == null) {
            return Result.error(400, "面试信息不能为空");
        }
        try {
            boolean result = interviewService.createInterview(interview);
            if (!result) {
                return Result.error(400, "发送面试邀请失败，请检查投递记录或必填信息");
            }
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(500, "发送面试邀请失败：" + e.getMessage());
        }
    }

    /**
     * 获取企业面试列表
     */
    @GetMapping("/interviews")
    public Result<Page<Interview>> getEnterpriseInterviews(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam Long enterpriseId,
            @RequestParam(required = false) Integer status) {
        Page<Interview> pageInfo = interviewService.getEnterpriseInterviews(page, size, enterpriseId, status);
        return Result.success(pageInfo);
    }

    /**
     * 更新面试状态
     */
    @PutMapping("/interview/status")
    public Result<Boolean> updateInterviewStatus(
            @RequestParam Long interviewId,
            @RequestParam Integer status) {
        boolean result = interviewService.updateInterviewStatus(interviewId, status);
        return Result.success(result);
    }

    /**
     * 更新面试结果
     */
    @PutMapping("/interview/result")
    public Result<Boolean> updateInterviewResult(
            @RequestParam Long interviewId,
            @RequestParam Integer result,
            @RequestParam(required = false) String feedback) {
        boolean updateResult = interviewService.updateInterviewResult(interviewId, result, feedback);
        return Result.success(updateResult);
    }

    /**
     * 搜索人才
     */
    @GetMapping("/search")
    public Result<List<Map<String, Object>>> searchTalents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String careerDirection,
            @RequestParam(required = false) String education,
            @RequestParam(required = false) String certification) {
        QueryWrapper<Resume> wrapper = new QueryWrapper<>();
        wrapper.eq("is_public", 1);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like("basic_info", kw)
                .or().like("skills", kw)
                .or().like("work_experience", kw)
                .or().like("project_experience", kw)
                .or().like("certificates", kw));
        }
        if (careerDirection != null && !careerDirection.trim().isEmpty()) {
            wrapper.like("basic_info", careerDirection.trim());
        }
        if (education != null && !education.trim().isEmpty()) {
            wrapper.like("education", education.trim());
        }
        if (certification != null && !certification.trim().isEmpty()) {
            wrapper.like("certificates", certification.trim());
        }

        List<Resume> resumes = resumeService.list(wrapper);
        List<Map<String, Object>> talents = new ArrayList<>();

        for (Resume resume : resumes) {
            Map<String, Object> talent = new HashMap<>();
            Map<String, Object> basicInfo = parseJsonObject(resume.getBasicInfo());
            List<Map<String, Object>> educationList = parseJsonArray(resume.getEducation());
            List<Map<String, Object>> workList = parseJsonArray(resume.getWorkExperience());
            List<Object> skillsRaw = parseJsonList(resume.getSkills());
            List<Map<String, Object>> certs = parseJsonArray(resume.getCertificates());

            String name = getString(basicInfo, "name", "未命名");
            String jobTitle = getString(basicInfo, "careerObjective", "");
            String city = getString(basicInfo, "location", "");

            talent.put("id", resume.getId());
            talent.put("name", name);
            talent.put("avatar", getString(basicInfo, "avatar", ""));
            talent.put("jobTitle", jobTitle);
            talent.put("city", city);
            talent.put("education", extractEducation(educationList));
            talent.put("experience", extractExperience(basicInfo, workList));
            talent.put("skills", extractSkills(skillsRaw));
            talent.put("intro", jobTitle);
            talent.put("expectedSalary", getString(basicInfo, "expectedSalary", "面议"));
            talent.put("certLevel", extractCertLevel(certs));
            talent.put("isOnline", false);
            talent.put("collected", false);
            talent.put("intention", "");
            talent.put("note", "");

            talents.add(talent);
        }

        return Result.success(talents);
    }

    private Map<String, Object> buildApplicationView(Application application) {
        Map<String, Object> row = new HashMap<>();
        if (application == null) return row;

        row.put("id", application.getId());
        row.put("jobId", application.getJobId());
        row.put("positionId", application.getJobId());
        row.put("userId", application.getUserId());
        row.put("enterpriseId", application.getEnterpriseId());
        row.put("status", application.getStatus());
        row.put("resumeUrl", application.getResumeUrl());
        row.put("applyTime", application.getApplyTime());
        row.put("createTime", application.getApplyTime() != null ? application.getApplyTime() : application.getCreateTime());
        row.put("feedback", application.getFeedback());
        row.put("feedbackTime", application.getFeedbackTime());

        if (application.getJobId() != null) {
            Job job = jobService.getById(application.getJobId());
            if (job != null) {
                row.put("positionName", firstNonBlank(job.getTitle(), ""));
            }
        }

        if (application.getUserId() != null) {
            UserPersonal user = userPersonalService.getUserById(application.getUserId());
            if (user != null) {
                row.put("userName", firstNonBlank(user.getName(), user.getUsername()));
                row.put("username", firstNonBlank(user.getUsername(), user.getName()));
                row.put("phone", firstNonBlank(user.getPhone(), ""));
                row.put("careerDirection", firstNonBlank(user.getCareerDirection(), ""));
            }

            Resume resume = resumeService.getByUserId(application.getUserId());
            if (resume != null) {
                row.put("resumeId", resume.getId());

                Map<String, Object> basicInfo = parseJsonObject(resume.getBasicInfo());
                List<Map<String, Object>> educationList = parseJsonArray(resume.getEducation());
                List<Map<String, Object>> workList = parseJsonArray(resume.getWorkExperience());

                String direction = firstNonBlank(
                        getString(basicInfo, "careerDirection", ""),
                        getString(basicInfo, "careerObjective", ""),
                        row.get("careerDirection") != null ? row.get("careerDirection").toString() : ""
                );
                row.put("careerDirection", direction);
                row.put("education", firstNonBlank(extractEducation(educationList), ""));
                row.put("workExperience", firstNonBlank(extractExperience(basicInfo, workList), ""));
            }
        }

        if (firstNonBlank(row.get("userName") != null ? row.get("userName").toString() : "").isEmpty()) {
            row.put("userName", application.getUserId() != null ? "用户#" + application.getUserId() : "");
        }
        if (firstNonBlank(row.get("positionName") != null ? row.get("positionName").toString() : "").isEmpty()) {
            row.put("positionName", application.getJobId() != null ? "职位#" + application.getJobId() : "");
        }
        row.put("careerDirection", firstNonBlank(row.get("careerDirection") != null ? row.get("careerDirection").toString() : "", ""));
        row.put("education", firstNonBlank(row.get("education") != null ? row.get("education").toString() : "", ""));
        row.put("workExperience", firstNonBlank(row.get("workExperience") != null ? row.get("workExperience").toString() : "", ""));

        return row;
    }

    private Map<String, Object> parseJsonObject(String json) {
        if (json == null || json.trim().isEmpty()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private List<Map<String, Object>> parseJsonArray(String json) {
        if (json == null || json.trim().isEmpty()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Object> parseJsonList(String json) {
        if (json == null || json.trim().isEmpty()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Object>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String extractEducation(List<Map<String, Object>> educationList) {
        if (educationList == null || educationList.isEmpty()) return "";
        Map<String, Object> edu = educationList.get(0);
        String degree = getString(edu, "degree", "");
        String school = getString(edu, "school", "");
        return !degree.isEmpty() ? degree : school;
    }

    private String extractExperience(Map<String, Object> basicInfo, List<Map<String, Object>> workList) {
        String workYears = getString(basicInfo, "workYears", "");
        if (!workYears.isEmpty()) return workYears + "年";
        if (workList != null && !workList.isEmpty()) return workList.size() + "段经历";
        return "经验不限";
    }

    private List<String> extractSkills(List<Object> skillsRaw) {
        List<String> skills = new ArrayList<>();
        if (skillsRaw == null) return skills;
        for (Object item : skillsRaw) {
            if (item instanceof String) {
                skills.add((String) item);
            } else if (item instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) item;
                String name = getString(map, "name", "");
                if (!name.isEmpty()) skills.add(name);
            }
        }
        return skills;
    }

    private String extractCertLevel(List<Map<String, Object>> certs) {
        if (certs == null || certs.isEmpty()) return "";
        Object levelObj = certs.get(0).get("level");
        if (levelObj == null) return "";
        int level = 0;
        try {
            level = Integer.parseInt(levelObj.toString());
        } catch (Exception e) {
            return "";
        }
        switch (level) {
            case 1:
                return "初级认证";
            case 2:
                return "中级认证";
            case 3:
                return "高级认证";
            default:
                return "专家认证";
        }
    }

    private String getString(Map<String, Object> map, String key, String defaultValue) {
        if (map == null) return defaultValue;
        Object val = map.get(key);
        return val != null ? val.toString() : defaultValue;
    }

    private String firstNonBlank(String... values) {
        if (values == null) return "";
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }
}
