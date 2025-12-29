package com.apparelcert.service.impl;

import com.apparelcert.entity.Resume;
import com.apparelcert.mapper.ResumeMapper;
import com.apparelcert.service.ResumeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 简历服务实现类
 */
@Service
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements ResumeService {

    @Autowired
    private ResumeMapper resumeMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Resume getByUserId(Long userId) {
        QueryWrapper<Resume> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return resumeMapper.selectOne(wrapper);
    }

    @Override
    public Map<String, Object> getResumeByUserId(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        Resume resume = getByUserId(userId);
        if (resume == null) {
            return null;
        }

        // 解析基本信息 JSON
        result.put("basicInfo", parseJsonObject(resume.getBasicInfo()));
        
        // 解析教育经历 JSON
        result.put("education", parseJsonArray(resume.getEducation()));
        
        // 解析工作经历 JSON
        result.put("workExperience", parseJsonArray(resume.getWorkExperience()));
        
        // 解析项目经验 JSON
        result.put("projectExperience", parseJsonArray(resume.getProjectExperience()));
        
        // 解析技能 JSON
        result.put("skills", parseJsonArray(resume.getSkills()));
        
        // 解析证书 JSON
        result.put("certificates", parseJsonArray(resume.getCertificates()));
        
        // 公开状态
        result.put("isPublic", resume.getIsPublic() != null && resume.getIsPublic() == 1);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveResumeData(Long userId, Map<String, Object> resumeData) {
        // 查找现有简历
        Resume resume = getByUserId(userId);
        boolean isNew = (resume == null);
        
        if (isNew) {
            resume = new Resume();
            resume.setUserId(userId);
        }

        // 保存基本信息为 JSON
        Object basicInfoObj = resumeData.get("basicInfo");
        resume.setBasicInfo(toJsonString(basicInfoObj != null ? basicInfoObj : new HashMap<>()));
        
        // 保存教育经历为 JSON
        resume.setEducation(toJsonString(resumeData.get("education")));
        
        // 保存工作经历为 JSON
        resume.setWorkExperience(toJsonString(resumeData.get("workExperience")));
        
        // 保存项目经验为 JSON
        resume.setProjectExperience(toJsonString(resumeData.get("projectExperience")));
        
        // 保存技能为 JSON
        resume.setSkills(toJsonString(resumeData.get("skills")));
        
        // 保存证书为 JSON
        resume.setCertificates(toJsonString(resumeData.get("certificates")));
        
        // 公开状态
        Boolean isPublic = (Boolean) resumeData.get("isPublic");
        resume.setIsPublic(isPublic != null && isPublic ? 1 : 0);

        if (isNew) {
            return resumeMapper.insert(resume) > 0;
        } else {
            return resumeMapper.updateById(resume) > 0;
        }
    }

    @Override
    public boolean saveOrUpdateResume(Resume resume) {
        return this.saveOrUpdate(resume);
    }

    @Override
    public void exportToPdf(Long userId, HttpServletResponse response) {
        Resume resume = getByUserId(userId);
        if (resume == null) {
            return;
        }

        try {
            // 解析基本信息
            Map<String, Object> basicInfo = parseJsonObject(resume.getBasicInfo());
            String name = (String) basicInfo.getOrDefault("name", "简历");

            // 设置响应头
            response.setContentType("application/pdf");
            String fileName = URLEncoder.encode("简历_" + name + ".pdf", StandardCharsets.UTF_8.toString());
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            // 生成简单的文本格式PDF（使用纯文本模拟，实际项目应使用iText或其他PDF库）
            OutputStream out = response.getOutputStream();
            
            // 构建简历内容
            StringBuilder content = new StringBuilder();
            content.append("%PDF-1.4\n");
            content.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
            content.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
            content.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n");
            
            // 构建简历文本内容
            StringBuilder resumeText = new StringBuilder();
            resumeText.append("BT\n/F1 24 Tf\n50 750 Td\n(").append(escapeForPdf(name)).append(") Tj\n");
            
            int yPos = 720;
            
            // 联系方式
            String phone = (String) basicInfo.getOrDefault("phone", "");
            String email = (String) basicInfo.getOrDefault("email", "");
            if (!phone.isEmpty() || !email.isEmpty()) {
                resumeText.append("/F1 12 Tf\n50 ").append(yPos).append(" Td\n(").append(escapeForPdf(phone + " | " + email)).append(") Tj\n");
                yPos -= 30;
            }
            
            // 求职意向
            String careerObjective = (String) basicInfo.getOrDefault("careerObjective", "");
            if (!careerObjective.isEmpty()) {
                resumeText.append("/F1 12 Tf\n50 ").append(yPos).append(" Td\n(").append(escapeForPdf("求职意向: " + careerObjective)).append(") Tj\n");
                yPos -= 40;
            }
            
            // 教育背景
            List<Object> education = parseJsonArray(resume.getEducation());
            if (!education.isEmpty()) {
                resumeText.append("/F1 16 Tf\n50 ").append(yPos).append(" Td\n(Education) Tj\n");
                yPos -= 20;
                for (Object edu : education) {
                    if (edu instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> eduMap = (Map<String, Object>) edu;
                        String school = (String) eduMap.getOrDefault("school", "");
                        String major = (String) eduMap.getOrDefault("major", "");
                        resumeText.append("/F1 12 Tf\n50 ").append(yPos).append(" Td\n(").append(escapeForPdf(school + " - " + major)).append(") Tj\n");
                        yPos -= 20;
                    }
                }
                yPos -= 20;
            }
            
            resumeText.append("ET\n");
            
            String streamContent = resumeText.toString();
            content.append("4 0 obj\n<< /Length ").append(streamContent.length()).append(" >>\nstream\n");
            content.append(streamContent);
            content.append("endstream\nendobj\n");
            content.append("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
            content.append("xref\n0 6\n0000000000 65535 f \n");
            content.append("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n0\n%%EOF");
            
            out.write(content.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 转义PDF特殊字符
     */
    private String escapeForPdf(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("(", "\\(")
                   .replace(")", "\\)")
                   .replaceAll("[^\\x00-\\x7F]", "?"); // 简单处理非ASCII字符
    }

    @Override
    public boolean setPublicStatus(Long resumeId, Integer isPublic) {
        Resume resume = new Resume();
        resume.setId(resumeId);
        resume.setIsPublic(isPublic);
        return this.updateById(resume);
    }

    @Override
    public boolean setPublicStatusByUserId(Long userId, Integer isPublic) {
        Resume resume = getByUserId(userId);
        if (resume == null) {
            return false;
        }
        resume.setIsPublic(isPublic);
        return resumeMapper.updateById(resume) > 0;
    }

    /**
     * 将对象转换为 JSON 字符串
     */
    private String toJsonString(Object obj) {
        if (obj == null) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    /**
     * 解析 JSON 字符串为 Map
     */
    private Map<String, Object> parseJsonObject(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    /**
     * 解析 JSON 字符串为 List
     */
    private List<Object> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Object>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}
