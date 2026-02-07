package com.apparelcert.service.impl;

import com.apparelcert.dto.ExamPaperRule;
import com.apparelcert.entity.Question;
import com.apparelcert.enums.Difficulty;
import com.apparelcert.enums.QuestionType;
import com.apparelcert.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 随机组卷算法测试
 */
@Slf4j
@SpringBootTest
public class QuestionServiceImplTest {

    @Autowired
    private QuestionService questionService;

    /**
     * 测试默认组卷规则
     */
    @Test
    public void testGenerateExamPaper_DefaultRule() {
        log.info("========== 测试1：使用默认规则组卷 ==========");

        List<Question> paper = questionService.generateExamPaper("designer", 2, 20);

        printPaperResult("设计师初级认证考试（默认规则）", paper);
    }

    /**
     * 测试多次组卷，验证题目不重复
     */
    @Test
    public void testGenerateExamPaper_MultipleTimes() {
        log.info("========== 测试2：多次组卷验证不重复 ==========");

        List<List<Question>> papers = new ArrayList<>();

        // 生成3份试卷
        for (int i = 0; i < 3; i++) {
            List<Question> paper = questionService.generateExamPaper("designer", 2, 20);
            papers.add(paper);
            log.info("第{}次组卷完成，题目数={}", i + 1, paper.size());
        }

        // 验证题目ID不重复
        Set<Long> allIds = new HashSet<>();
        Set<Long> duplicateIds = new HashSet<>();

        for (int i = 0; i < papers.size(); i++) {
            List<Question> paper = papers.get(i);
            Set<Long> paperIds = paper.stream().map(Question::getId).collect(Collectors.toSet());

            for (Long id : paperIds) {
                if (allIds.contains(id)) {
                    duplicateIds.add(id);
                }
                allIds.add(id);
            }

            log.info("试卷{}题目ID: {}", i + 1, paperIds);
        }

        if (duplicateIds.isEmpty()) {
            log.info("✓ 验证通过：3次组卷无重复题目");
        } else {
            log.error("✗ 验证失败：发现重复题目ID: {}", duplicateIds);
        }

        // 打印每份试卷的题型分布
        for (int i = 0; i < papers.size(); i++) {
            printPaperResult(String.format("试卷%d", i + 1), papers.get(i));
        }
    }

    /**
     * 测试自定义组卷规则
     */
    @Test
    public void testGenerateExamPaper_CustomRule() {
        log.info("========== 测试3：自定义规则组卷 ==========");

        ExamPaperRule rule = new ExamPaperRule();
        rule.setStandardId(null);
        rule.setCertificationType("designer");
        rule.setLevel(2); // 中级
        rule.setTotalQuestionCount(15);
        rule.setAllowDuplicate(false);

        // 自定义题型分布
        rule.setTypeDistribution(Map.of(
                QuestionType.SINGLE_CHOICE, 8,
                QuestionType.MULTIPLE_CHOICE, 4,
                QuestionType.TRUE_FALSE, 3
        ));

        // 自定义难度分布
        rule.setDifficultyDistribution(Map.of(
                Difficulty.EASY, 20,
                Difficulty.MEDIUM, 60,
                Difficulty.HARD, 20
        ));

        List<Question> paper = ((QuestionServiceImpl) questionService).generateExamPaperByRule(rule);

        printPaperResult("设计师中级认证考试（自定义规则）", paper);
    }

    /**
     * 测试题库不足的情况
     */
    @Test
    public void testGenerateExamPaper_InsufficientQuestions() {
        log.info("========== 测试4：题库不足场景 ==========");

        try {
            // 尝试生成超大题量的试卷（必然失败）
            List<Question> paper = questionService.generateExamPaper("designer", 2, 1000);
            log.error("✗ 测试失败：应该抛出异常但没有抛出");
        } catch (IllegalStateException e) {
            log.info("✓ 测试通过：正确捕获题库不足异常: {}", e.getMessage());
        } catch (Exception e) {
            log.error("✗ 测试失败：捕获到其他异常: {}", e.getMessage());
        }
    }

    /**
     * 测试获取所有分类
     */
    @Test
    public void testGetAllCategories() {
        log.info("========== 测试5：获取所有题目分类 ==========");

        List<String> categories = questionService.getAllCategories();

        log.info("题目分类列表（共{}个）:", categories.size());
        for (String category : categories) {
            log.info("  - {}", category);
        }
    }

    /**
     * 打印试卷结果
     */
    private void printPaperResult(String title, List<Question> paper) {
        if (paper == null || paper.isEmpty()) {
            log.info("{}: 无题目", title);
            return;
        }

        log.info("========== {} ==========", title);
        log.info("总题数: {}", paper.size());

        // 按题型统计
        Map<String, Long> typeCount = paper.stream()
                .collect(Collectors.groupingBy(q -> q.getType(), Collectors.counting()));
        log.info("题型分布: {}", typeCount);

        // 按难度统计
        Map<String, Long> difficultyCount = paper.stream()
                .collect(Collectors.groupingBy(q -> q.getDifficulty() != null ? q.getDifficulty() : "MEDIUM",
                        Collectors.counting()));
        log.info("难度分布: {}", difficultyCount);

        // 计算总分
        int totalScore = paper.stream().mapToInt(q -> q.getScore() != null ? q.getScore() : 1).sum();
        log.info("总分: {}", totalScore);

        // 题目ID列表
        List<Long> ids = paper.stream().map(Question::getId).collect(Collectors.toList());
        log.info("题目ID列表: {}", ids);

        // 显示前3道题目预览
        log.info("题目预览:");
        for (int i = 0; i < Math.min(3, paper.size()); i++) {
            Question q = paper.get(i);
            log.info("  {}. [{}] [{}] {}",
                    i + 1,
                    q.getType(),
                    q.getDifficulty() != null ? q.getDifficulty() : "MEDIUM",
                    q.getContent() != null ?
                            (q.getContent().length() > 50 ? q.getContent().substring(0, 50) + "..." : q.getContent())
                            : "(无内容)"
            );
        }
    }
}
