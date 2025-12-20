package com.apparelcert.service.impl;

import com.apparelcert.entity.Question;
import com.apparelcert.mapper.QuestionMapper;
import com.apparelcert.service.QuestionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 题目服务实现类
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public Page<Question> pageQuery(Integer page, Integer size, String keyword, String type, String difficulty, String category) {
        Page<Question> pageInfo = new Page<>(page, size);
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("content", keyword);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            wrapper.eq("difficulty", difficulty);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq("category", category);
        }
        
        wrapper.orderByAsc("category");
        wrapper.orderByAsc("difficulty");
        return questionMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public Question getById(Long id) {
        return questionMapper.selectById(id);
    }

    @Override
    public boolean saveOrUpdateQuestion(Question question) {
        return this.saveOrUpdate(question);
    }

    @Override
    public boolean deleteQuestion(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean deleteBatchQuestions(List<Long> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public List<Question> generateExamPaper(String certificationType, Integer level, Integer questionCount) {
        // TODO: 实现按条件随机生成试卷的逻辑
        return null;
    }

    @Override
    public List<Question> getByCertification(String certificationType) {
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        wrapper.eq("applicable_certification", certificationType);
        return questionMapper.selectList(wrapper);
    }

    @Override
    public List<String> getAllCategories() {
        // TODO: 实现获取所有题目分类的逻辑
        return null;
    }
}