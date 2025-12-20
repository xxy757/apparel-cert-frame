package com.apparelcert.service.impl;

import com.apparelcert.entity.Resume;
import com.apparelcert.mapper.ResumeMapper;
import com.apparelcert.service.ResumeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 简历服务实现类
 */
@Service
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements ResumeService {

    @Autowired
    private ResumeMapper resumeMapper;

    @Override
    public Resume getByUserId(Long userId) {
        QueryWrapper<Resume> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return resumeMapper.selectOne(wrapper);
    }

    @Override
    public boolean saveOrUpdateResume(Resume resume) {
        return this.saveOrUpdate(resume);
    }

    @Override
    public String exportToPdf(Long resumeId) {
        // TODO: 实现简历导出为PDF功能
        return "/pdf/resume_" + resumeId + ".pdf";
    }

    @Override
    public boolean setPublicStatus(Long resumeId, Integer isPublic) {
        Resume resume = new Resume();
        resume.setId(resumeId);
        resume.setIsPublic(isPublic);
        return this.updateById(resume);
    }
}