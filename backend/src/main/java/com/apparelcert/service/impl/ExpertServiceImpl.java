package com.apparelcert.service.impl;

import com.apparelcert.entity.Expert;
import com.apparelcert.mapper.ExpertMapper;
import com.apparelcert.service.ExpertService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 评审专家服务实现类
 */
@Service
public class ExpertServiceImpl extends ServiceImpl<ExpertMapper, Expert> implements ExpertService {

    @Autowired
    private ExpertMapper expertMapper;

    @Override
    public boolean addExpert(Expert expert) {
        // 生成专家编号
        expert.setExpertCode("EXP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        expert.setStatus(1); // 默认启用
        expert.setTotalReviews(0);
        return this.save(expert);
    }

    @Override
    public boolean updateExpert(Expert expert) {
        return this.updateById(expert);
    }

    @Override
    public boolean deleteExpert(Long expertId) {
        return this.removeById(expertId);
    }

    @Override
    public Expert getExpertDetail(Long expertId) {
        return expertMapper.selectById(expertId);
    }

    @Override
    public Page<Expert> pageQuery(Integer page, Integer size, String keyword, String specialty, Integer status) {
        Page<Expert> pageInfo = new Page<>(page, size);
        QueryWrapper<Expert> wrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword)
                .or().like("expert_code", keyword)
                .or().like("organization", keyword));
        }
        if (specialty != null && !specialty.isEmpty()) {
            wrapper.like("specialties", specialty);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("create_time");
        return expertMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public List<Expert> getExpertsBySpecialty(String specialty) {
        QueryWrapper<Expert> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.like("specialties", specialty);
        return expertMapper.selectList(wrapper);
    }

    @Override
    public boolean updateExpertStatus(Long expertId, Integer status) {
        Expert expert = new Expert();
        expert.setId(expertId);
        expert.setStatus(status);
        return this.updateById(expert);
    }

    @Override
    public List<Expert> getAvailableExperts(String specialty, Integer reviewLevel) {
        QueryWrapper<Expert> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        if (specialty != null && !specialty.isEmpty()) {
            wrapper.like("specialties", specialty);
        }
        if (reviewLevel != null) {
            wrapper.ge("review_level", reviewLevel);
        }
        wrapper.orderByAsc("total_reviews"); // 优先分配评审数少的专家
        return expertMapper.selectList(wrapper);
    }

    @Override
    public boolean updateReviewCount(Long expertId) {
        Expert expert = this.getById(expertId);
        if (expert != null) {
            expert.setTotalReviews((expert.getTotalReviews() != null ? expert.getTotalReviews() : 0) + 1);
            return this.updateById(expert);
        }
        return false;
    }
}
