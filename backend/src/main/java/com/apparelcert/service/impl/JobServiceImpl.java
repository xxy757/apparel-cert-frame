package com.apparelcert.service.impl;

import com.apparelcert.entity.Certification;
import com.apparelcert.entity.Job;
import com.apparelcert.entity.UserEnterprise;
import com.apparelcert.mapper.JobMapper;
import com.apparelcert.mapper.UserEnterpriseMapper;
import com.apparelcert.service.CertificationService;
import com.apparelcert.service.JobService;
import com.apparelcert.service.ResumeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 岗位服务实现类
 */
@Service
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements JobService {

    private static final int CAREER_SCORE_MAX = 35;
    private static final int SKILL_SCORE_MAX = 35;
    private static final int CERTIFICATION_SCORE_MAX = 15;
    private static final int LOCATION_SCORE_MAX = 10;
    private static final int POPULARITY_SCORE_MAX = 5;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_PAGE_NUM = 1;

    @Autowired
    private JobMapper jobMapper;

    @Autowired(required = false)
    private UserEnterpriseMapper userEnterpriseMapper;
    
    @Autowired(required = false)
    private ResumeService resumeService;
    
    @Autowired(required = false)
    private CertificationService certificationService;

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
        Page<Job> resultPage = jobMapper.selectPage(pageInfo, wrapper);
        enrichEnterpriseInfo(resultPage.getRecords());
        return resultPage;
    }

    @Override
    public Job getById(Long id) {
        Job job = jobMapper.selectById(id);
        enrichEnterpriseInfo(job);
        return job;
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
        List<Job> jobs = jobMapper.selectList(wrapper);
        enrichEnterpriseInfo(jobs);
        return jobs;
    }
    
    @Override
    public Map<String, Object> getSmartRecommendedJobs(Long userId, Integer page, Integer size) {
        int safePage = (page == null || page < 1) ? DEFAULT_PAGE_NUM : page;
        int safeSize = (size == null || size < 1) ? DEFAULT_PAGE_SIZE : size;

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> recommendedJobs = new ArrayList<>();
        
        // 获取用户简历信息
        Map<String, Object> userProfile = getUserProfile(userId);
        String careerDirection = asString(userProfile.get("careerDirection"));
        List<String> skills = normalizeTerms(getListFromProfile(userProfile, "skills"));
        List<String> certifications = mergeAndNormalizeTerms(
            getListFromProfile(userProfile, "certifications"),
            getListFromProfile(userProfile, "certificates")
        );
        String preferredLocation = firstNonBlank(
            asString(userProfile.get("preferredLocation")),
            asString(userProfile.get("location"))
        );

        boolean hasProfileSignals = !isBlank(careerDirection)
            || !skills.isEmpty()
            || !certifications.isEmpty()
            || !isBlank(preferredLocation);
        
        // 查询所有招聘中的岗位
        QueryWrapper<Job> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        List<Job> allJobs = jobMapper.selectList(wrapper);
        enrichEnterpriseInfo(allJobs);

        int maxViews = allJobs.stream()
            .mapToInt(job -> safeInt(job != null ? job.getViews() : 0))
            .max().orElse(0);
        int maxApplications = allJobs.stream()
            .mapToInt(job -> safeInt(job != null ? job.getApplications() : 0))
            .max().orElse(0);
        
        // 计算每个岗位的匹配度
        for (Job job : allJobs) {
            MatchResult matchResult = calculateMatchResult(
                job,
                careerDirection,
                skills,
                certifications,
                preferredLocation,
                maxViews,
                maxApplications,
                hasProfileSignals
            );
            
            Map<String, Object> jobWithScore = new HashMap<>();
            jobWithScore.put("job", job);
            jobWithScore.put("matchScore", matchResult.score);
            jobWithScore.put("matchReasons", matchResult.reasons);
            recommendedJobs.add(jobWithScore);
        }
        
        // 按匹配度排序
        recommendedJobs.sort((a, b) -> {
            int scoreCompare = Integer.compare((Integer) b.get("matchScore"), (Integer) a.get("matchScore"));
            if (scoreCompare != 0) {
                return scoreCompare;
            }

            Job jobB = (Job) b.get("job");
            Job jobA = (Job) a.get("job");
            int hotB = safeInt(jobB != null ? jobB.getApplications() : 0) * 2
                + safeInt(jobB != null ? jobB.getViews() : 0);
            int hotA = safeInt(jobA != null ? jobA.getApplications() : 0) * 2
                + safeInt(jobA != null ? jobA.getViews() : 0);
            return Integer.compare(hotB, hotA);
        });
        
        // 分页
        int start = (safePage - 1) * safeSize;
        int end = Math.min(start + safeSize, recommendedJobs.size());
        List<Map<String, Object>> pagedJobs = start < recommendedJobs.size() ? 
            new ArrayList<>(recommendedJobs.subList(start, end)) : new ArrayList<>();
        
        result.put("records", pagedJobs);
        result.put("total", recommendedJobs.size());
        result.put("page", safePage);
        result.put("size", safeSize);
        
        return result;
    }
    
    /**
     * 获取用户画像
     */
    private Map<String, Object> getUserProfile(Long userId) {
        Map<String, Object> profile = new HashMap<>();
        if (userId == null || userId <= 0) {
            return profile;
        }
        
        if (resumeService != null) {
            Map<String, Object> resumeData = resumeService.getResumeByUserId(userId);
            if (resumeData != null) {
                Map<String, Object> basicInfo = toMap(resumeData.get("basicInfo"));
                if (!basicInfo.isEmpty()) {
                    profile.put("careerDirection", firstNonBlank(
                        asString(basicInfo.get("careerObjective")),
                        asString(basicInfo.get("careerDirection"))
                    ));
                    String location = firstNonBlank(
                        asString(basicInfo.get("preferredLocation")),
                        asString(basicInfo.get("location"))
                    );
                    profile.put("preferredLocation", location);
                    profile.put("location", location);
                }
                profile.put("skills", resumeData.get("skills"));
                profile.put("certificates", resumeData.get("certificates"));
            }
        }
        
        // 获取用户认证信息
        if (certificationService != null) {
            List<Certification> certs = certificationService.getByUserId(userId);
            if (certs != null) {
                List<String> certTypes = certs.stream()
                    .filter(Objects::nonNull)
                    .filter(c -> Integer.valueOf(3).equals(c.getStatus())) // 已通过的认证
                    .map(Certification::getCertificationType)
                    .filter(s -> !isBlank(s))
                    .collect(Collectors.toList());
                profile.put("certifications", certTypes);
            }
        }
        
        return profile;
    }
    
    private MatchResult calculateMatchResult(Job job,
                                             String careerDirection,
                                             List<String> skills,
                                             List<String> certifications,
                                             String preferredLocation,
                                             int maxViews,
                                             int maxApplications,
                                             boolean hasProfileSignals) {
        int careerScore = calculateCareerScore(job, careerDirection);
        TermMatchResult skillResult = calculateTermScore(
            skills,
            mergeText(job != null ? job.getSkills() : null,
                job != null ? job.getRequirements() : null,
                job != null ? job.getDescription() : null,
                job != null ? job.getTitle() : null,
                job != null ? job.getType() : null),
            SKILL_SCORE_MAX,
            10
        );
        TermMatchResult certResult = calculateTermScore(
            certifications,
            mergeText(job != null ? job.getCertificationRequirement() : null,
                job != null ? job.getRequirements() : null,
                job != null ? job.getType() : null,
                job != null ? job.getTitle() : null,
                job != null ? job.getDescription() : null),
            CERTIFICATION_SCORE_MAX,
            6
        );
        int locationScore = calculateLocationScore(job, preferredLocation);
        int popularityScore = calculatePopularityScore(job, maxViews, maxApplications);

        int totalScore = careerScore + skillResult.score + certResult.score + locationScore + popularityScore;

        List<String> reasons = new ArrayList<>();
        if (careerScore > 0) {
            reasons.add(careerScore >= CAREER_SCORE_MAX ? "职业方向高度匹配" : "职业方向相关");
        }
        if (!skillResult.matchedTerms.isEmpty()) {
            reasons.add("技能匹配: " + String.join("、", limitTerms(skillResult.matchedTerms, 2)));
        }
        if (!certResult.matchedTerms.isEmpty()) {
            reasons.add("认证要求匹配: " + certResult.matchedTerms.get(0));
        }
        if (locationScore > 0) {
            reasons.add("工作地点匹配");
        }
        if (popularityScore >= 3) {
            reasons.add("岗位热度较高");
        }

        if (!hasProfileSignals) {
            totalScore = Math.max(totalScore, 35 + popularityScore * 6);
            reasons.add(0, "根据岗位热度智能推荐");
        } else if (totalScore == 0) {
            totalScore = 12 + popularityScore * 2;
            reasons.add("综合岗位信息推荐");
        }

        if (reasons.isEmpty()) {
            reasons.add("热门岗位推荐");
        }

        List<String> finalReasons = reasons.stream()
            .filter(r -> !isBlank(r))
            .distinct()
            .limit(3)
            .collect(Collectors.toList());

        return new MatchResult(Math.min(100, totalScore), finalReasons);
    }

    private int calculateCareerScore(Job job, String careerDirection) {
        if (job == null || isBlank(careerDirection)) {
            return 0;
        }

        if (containsLoose(job.getType(), careerDirection) || containsLoose(job.getTitle(), careerDirection)) {
            return CAREER_SCORE_MAX;
        }

        List<String> directionTerms = splitTerms(careerDirection);
        String jobText = mergeText(job.getType(), job.getTitle(), job.getDescription(), job.getRequirements());
        if (isBlank(jobText) || directionTerms.isEmpty()) {
            return 0;
        }

        long matchedCount = directionTerms.stream()
            .filter(term -> containsLoose(jobText, term))
            .count();
        if (matchedCount <= 0) {
            return 0;
        }

        double ratio = matchedCount * 1.0 / directionTerms.size();
        return Math.min(CAREER_SCORE_MAX, Math.max(16, (int) Math.round(CAREER_SCORE_MAX * ratio)));
    }

    private TermMatchResult calculateTermScore(List<String> userTerms, String jobText, int maxScore, int minScoreWhenMatched) {
        if (userTerms == null || userTerms.isEmpty() || isBlank(jobText)) {
            return TermMatchResult.empty();
        }

        List<String> matchedTerms = userTerms.stream()
            .filter(term -> containsLoose(jobText, term))
            .distinct()
            .collect(Collectors.toList());
        if (matchedTerms.isEmpty()) {
            return TermMatchResult.empty();
        }

        double ratio = matchedTerms.size() * 1.0 / userTerms.size();
        int score = Math.max(minScoreWhenMatched, (int) Math.round(maxScore * ratio));
        return new TermMatchResult(Math.min(maxScore, score), matchedTerms);
    }

    private int calculateLocationScore(Job job, String preferredLocation) {
        if (job == null || isBlank(preferredLocation) || isBlank(job.getLocation())) {
            return 0;
        }

        if (containsLoose(job.getLocation(), preferredLocation)
            || containsLoose(preferredLocation, job.getLocation())) {
            return LOCATION_SCORE_MAX;
        }

        List<String> preferredTerms = splitTerms(preferredLocation);
        List<String> locationTerms = splitTerms(job.getLocation());
        for (String preferredTerm : preferredTerms) {
            for (String locationTerm : locationTerms) {
                if (containsLoose(preferredTerm, locationTerm) || containsLoose(locationTerm, preferredTerm)) {
                    return 7;
                }
            }
        }
        return 0;
    }

    private int calculatePopularityScore(Job job, int maxViews, int maxApplications) {
        if (job == null) {
            return 0;
        }

        double viewRatio = maxViews > 0 ? safeInt(job.getViews()) * 1.0 / maxViews : 0;
        double applyRatio = maxApplications > 0 ? safeInt(job.getApplications()) * 1.0 / maxApplications : 0;
        double urgentBoost = Integer.valueOf(1).equals(job.getIsUrgent()) ? 0.15 : 0;
        double hotness = Math.min(1.0, viewRatio * 0.4 + applyRatio * 0.6 + urgentBoost);

        return (int) Math.round(hotness * POPULARITY_SCORE_MAX);
    }

    private List<String> mergeAndNormalizeTerms(List<String>... sources) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (sources != null) {
            for (List<String> source : sources) {
                if (source == null) {
                    continue;
                }
                source.stream()
                    .filter(s -> !isBlank(s))
                    .map(String::trim)
                    .forEach(merged::add);
            }
        }
        return new ArrayList<>(merged);
    }

    private List<String> normalizeTerms(List<String> terms) {
        if (terms == null) {
            return new ArrayList<>();
        }
        return terms.stream()
            .filter(s -> !isBlank(s))
            .map(String::trim)
            .distinct()
            .collect(Collectors.toList());
    }

    private List<String> limitTerms(List<String> terms, int limit) {
        if (terms == null || terms.isEmpty()) {
            return new ArrayList<>();
        }
        return terms.stream().limit(Math.max(1, limit)).collect(Collectors.toList());
    }

    private List<String> getListFromProfile(Map<String, Object> profile, String key) {
        if (profile == null || isBlank(key)) {
            return new ArrayList<>();
        }
        return parseToTerms(profile.get(key));
    }

    private List<String> parseToTerms(Object value) {
        LinkedHashSet<String> terms = new LinkedHashSet<>();
        if (value == null) {
            return new ArrayList<>();
        }

        if (value instanceof Collection<?>) {
            for (Object item : (Collection<?>) value) {
                appendTermFromObject(item, terms);
            }
        } else {
            appendTermFromObject(value, terms);
        }
        return new ArrayList<>(terms);
    }

    private void appendTermFromObject(Object item, Set<String> terms) {
        if (item == null) {
            return;
        }
        if (item instanceof String) {
            appendRawTerm((String) item, terms);
            return;
        }
        if (item instanceof Number || item instanceof Boolean) {
            appendRawTerm(String.valueOf(item), terms);
            return;
        }
        if (item instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) item;
            appendRawTerm(firstNonBlank(
                asString(map.get("name")),
                asString(map.get("title")),
                asString(map.get("skill")),
                asString(map.get("certificationType")),
                asString(map.get("type")),
                asString(map.get("value")),
                asString(map.get("label")),
                asString(map.get("text"))
            ), terms);
        }
    }

    private void appendRawTerm(String rawTerm, Set<String> terms) {
        if (isBlank(rawTerm)) {
            return;
        }

        String trimmed = rawTerm.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        terms.add(trimmed);
        String withoutBrackets = removeBracketContent(trimmed);
        if (!isBlank(withoutBrackets)) {
            terms.add(withoutBrackets);
        }

        for (String part : trimmed.split("[,，;；、|/\\\\\\s]+")) {
            String normalizedPart = part != null ? part.trim() : "";
            if (isUsefulTerm(normalizedPart)) {
                terms.add(normalizedPart);
            }
        }
    }

    private List<String> splitTerms(String text) {
        if (isBlank(text)) {
            return new ArrayList<>();
        }

        LinkedHashSet<String> result = new LinkedHashSet<>();
        appendRawTerm(text, result);
        return new ArrayList<>(result);
    }

    private Map<String, Object> toMap(Object value) {
        if (!(value instanceof Map<?, ?>)) {
            return new HashMap<>();
        }
        Map<?, ?> source = (Map<?, ?>) value;
        Map<String, Object> result = new HashMap<>();
        source.forEach((k, v) -> {
            if (k != null) {
                result.put(String.valueOf(k), v);
            }
        });
        return result;
    }

    private String mergeText(String... texts) {
        if (texts == null || texts.length == 0) {
            return "";
        }
        return Arrays.stream(texts)
            .filter(s -> !isBlank(s))
            .collect(Collectors.joining(" "));
    }

    private boolean containsLoose(String source, String target) {
        if (isBlank(source) || isBlank(target)) {
            return false;
        }

        String normalizedSource = normalizeForMatch(source);
        String normalizedTarget = normalizeForMatch(target);
        if (normalizedSource.isEmpty() || normalizedTarget.isEmpty()) {
            return false;
        }

        return normalizedSource.contains(normalizedTarget)
            || normalizedTarget.contains(normalizedSource);
    }

    private String normalizeForMatch(String text) {
        if (text == null) {
            return "";
        }
        return text.trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[\\s\\u3000]+", "");
    }

    private String removeBracketContent(String text) {
        if (isBlank(text)) {
            return "";
        }
        return text.replaceAll("（[^）]*）", "")
            .replaceAll("\\([^)]*\\)", "")
            .trim();
    }

    private boolean isUsefulTerm(String term) {
        if (isBlank(term)) {
            return false;
        }
        if (term.length() >= 2) {
            return true;
        }
        return term.matches("[A-Za-z0-9]+");
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class MatchResult {
        private final int score;
        private final List<String> reasons;

        private MatchResult(int score, List<String> reasons) {
            this.score = score;
            this.reasons = reasons;
        }
    }

    private static class TermMatchResult {
        private final int score;
        private final List<String> matchedTerms;

        private TermMatchResult(int score, List<String> matchedTerms) {
            this.score = score;
            this.matchedTerms = matchedTerms;
        }

        private static TermMatchResult empty() {
            return new TermMatchResult(0, new ArrayList<>());
        }
    }

    @Override
    public List<Job> searchJobs(String keyword) {
        QueryWrapper<Job> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.and(w -> w.like("title", keyword).or().like("description", keyword));
        wrapper.orderByDesc("create_time");
        wrapper.last("limit 20");
        List<Job> jobs = jobMapper.selectList(wrapper);
        enrichEnterpriseInfo(jobs);
        return jobs;
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

    private void enrichEnterpriseInfo(List<Job> jobs) {
        if (jobs == null || jobs.isEmpty() || userEnterpriseMapper == null) {
            return;
        }

        Set<Long> enterpriseIds = jobs.stream()
            .map(Job::getEnterpriseId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (enterpriseIds.isEmpty()) {
            return;
        }

        QueryWrapper<UserEnterprise> wrapper = new QueryWrapper<>();
        wrapper.in("id", enterpriseIds);
        List<UserEnterprise> enterprises = userEnterpriseMapper.selectList(wrapper);
        if (enterprises == null || enterprises.isEmpty()) {
            return;
        }

        Map<Long, UserEnterprise> enterpriseMap = enterprises.stream()
            .collect(Collectors.toMap(UserEnterprise::getId, e -> e, (a, b) -> a));

        for (Job job : jobs) {
            if (job == null || job.getEnterpriseId() == null) {
                continue;
            }
            UserEnterprise enterprise = enterpriseMap.get(job.getEnterpriseId());
            if (enterprise != null) {
                job.setCompanyName(enterprise.getCompanyName());
                job.setCompanyLogo(enterprise.getLogo());
            }
        }
    }

    private void enrichEnterpriseInfo(Job job) {
        if (job == null) {
            return;
        }
        enrichEnterpriseInfo(Collections.singletonList(job));
    }
}
