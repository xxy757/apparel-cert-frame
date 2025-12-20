package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Question;
import com.apparelcert.service.QuestionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目控制器
 */
@RestController
@RequestMapping("/api/admin/certification/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    /**
     * 分页查询题目列表
     */
    @GetMapping
    public Result<Page<Question>> getQuestionList(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String category) {
        Page<Question> pageInfo = questionService.pageQuery(page, size, keyword, type, difficulty, category);
        return Result.success(pageInfo);
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/detail")
    public Result<Question> getQuestionDetail(@RequestParam Long questionId) {
        Question question = questionService.getById(questionId);
        return Result.success(question);
    }

    /**
     * 保存或更新题目
     */
    @PostMapping
    public Result<Boolean> saveOrUpdateQuestion(@RequestBody Question question) {
        boolean result = questionService.saveOrUpdateQuestion(question);
        return Result.success(result);
    }

    /**
     * 删除题目
     */
    @DeleteMapping
    public Result<Boolean> deleteQuestion(@RequestParam Long questionId) {
        boolean result = questionService.deleteQuestion(questionId);
        return Result.success(result);
    }

    /**
     * 批量删除题目
     */
    @DeleteMapping("/batch")
    public Result<Boolean> deleteBatchQuestions(@RequestBody List<Long> ids) {
        boolean result = questionService.deleteBatchQuestions(ids);
        return Result.success(result);
    }

    /**
     * 生成试卷
     */
    @GetMapping("/generate-exam")
    public Result<List<Question>> generateExamPaper(
            @RequestParam String certificationType,
            @RequestParam Integer level,
            @RequestParam Integer questionCount) {
        List<Question> questions = questionService.generateExamPaper(certificationType, level, questionCount);
        return Result.success(questions);
    }

    /**
     * 根据认证类型获取题目
     */
    @GetMapping("/by-certification")
    public Result<List<Question>> getQuestionsByCertification(@RequestParam String certificationType) {
        List<Question> questions = questionService.getByCertification(certificationType);
        return Result.success(questions);
    }

    /**
     * 获取所有题目分类
     */
    @GetMapping("/categories")
    public Result<List<String>> getAllCategories() {
        List<String> categories = questionService.getAllCategories();
        return Result.success(categories);
    }
}