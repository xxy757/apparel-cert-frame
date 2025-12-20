package com.apparelcert.service;

import com.apparelcert.entity.Question;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 题目服务接口
 */
public interface QuestionService extends IService<Question> {
    /**
     * 分页查询题目列表
     */
    Page<Question> pageQuery(Integer page, Integer size, String keyword, String type, String difficulty, String category);
    
    /**
     * 获取题目详情
     */
    Question getById(Long id);
    
    /**
     * 保存或更新题目
     */
    boolean saveOrUpdateQuestion(Question question);
    
    /**
     * 删除题目
     */
    boolean deleteQuestion(Long id);
    
    /**
     * 批量删除题目
     */
    boolean deleteBatchQuestions(List<Long> ids);
    
    /**
     * 按条件随机生成试卷
     */
    List<Question> generateExamPaper(String certificationType, Integer level, Integer questionCount);
    
    /**
     * 根据认证类型获取题目
     */
    List<Question> getByCertification(String certificationType);
    
    /**
     * 获取所有题目分类
     */
    List<String> getAllCategories();
}