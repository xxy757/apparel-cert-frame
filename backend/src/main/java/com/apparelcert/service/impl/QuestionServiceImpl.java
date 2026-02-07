package com.apparelcert.service.impl;

import com.apparelcert.dto.ExamPaperRule;
import com.apparelcert.entity.Question;
import com.apparelcert.enums.Difficulty;
import com.apparelcert.enums.QuestionType;
import com.apparelcert.mapper.QuestionMapper;
import com.apparelcert.service.QuestionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目服务实现类
 */
@Slf4j
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
        // 创建默认组卷规则
        ExamPaperRule rule = ExamPaperRule.createDefaultRule(null, certificationType, level);
        if (questionCount != null && questionCount > 0) {
            rule.setTotalQuestionCount(questionCount);
        }

        return generateExamPaperByRule(rule);
    }

    /**
     * 根据组卷规则生成试卷
     */
    public List<Question> generateExamPaperByRule(ExamPaperRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("组卷规则不能为空");
        }

        log.info("开始生成试卷: certificationType={}, level={}, totalQuestions={}",
                rule.getCertificationType(), rule.getLevel(), rule.getTotalQuestionCount());

        List<Question> paper = new ArrayList<>();
        Set<Long> selectedIds = new HashSet<>();

        // 按题型抽取题目
        Map<QuestionType, Integer> typeDistribution = rule.getTypeDistribution();
        if (typeDistribution == null || typeDistribution.isEmpty()) {
            throw new IllegalStateException("题型分布不能为空");
        }

        for (Map.Entry<QuestionType, Integer> entry : typeDistribution.entrySet()) {
            QuestionType type = entry.getKey();
            Integer count = entry.getValue();

            if (count == null || count <= 0) {
                continue;
            }

            // 按难度分布抽取该题型的题目
            List<Question> questions = generateQuestionsByTypeAndDifficulty(
                    rule.getStandardId(),
                    rule.getCertificationType(),
                    type,
                    count,
                    rule.getDifficultyDistribution(),
                    selectedIds
            );

            paper.addAll(questions);
        }

        // 打乱题目顺序
        Collections.shuffle(paper);

        log.info("试卷生成完成: 总题数={}, 题型分布={}",
                paper.size(),
                paper.stream().collect(Collectors.groupingBy(q -> q.getType(), Collectors.counting())));

        return paper;
    }

    /**
     * 按题型和难度分布抽取题目
     */
    private List<Question> generateQuestionsByTypeAndDifficulty(
            Long standardId,
            String certificationType,
            QuestionType questionType,
            Integer requiredCount,
            Map<Difficulty, Integer> difficultyDistribution,
            Set<Long> selectedIds) {

        List<Question> result = new ArrayList<>();

        // 获取该题型的所有题目
        List<Question> allQuestions = getQuestionsByType(certificationType, questionType);

        if (allQuestions.isEmpty()) {
            log.warn("题库中没有{}类型的题目", questionType.getDescription());
            return result;
        }

        // 按难度分组
        Map<Difficulty, List<Question>> questionsByDifficulty = allQuestions.stream()
                .collect(Collectors.groupingBy(q -> {
                    Difficulty difficulty = Difficulty.fromCode(q.getDifficulty());
                    return difficulty != null ? difficulty : Difficulty.MEDIUM;
                }));

        // 根据难度分布计算每种难度需要抽取的数量
        Map<Difficulty, Integer> countByDifficulty = calculateCountByDifficulty(
                requiredCount, difficultyDistribution);

        // 从每种难度中随机抽取
        for (Map.Entry<Difficulty, Integer> entry : countByDifficulty.entrySet()) {
            Difficulty difficulty = entry.getKey();
            Integer count = entry.getValue();

            if (count == null || count <= 0) {
                continue;
            }

            List<Question> difficultyQuestions = questionsByDifficulty.getOrDefault(difficulty, new ArrayList<>());

            // 过滤掉已选择的题目
            List<Question> availableQuestions = difficultyQuestions.stream()
                    .filter(q -> !selectedIds.contains(q.getId()))
                    .collect(Collectors.toList());

            if (availableQuestions.size() < count) {
                log.error("难度为{}的{}类型题目不足：需要{}道，可用{}道",
                        difficulty.getDescription(), questionType.getDescription(), count, availableQuestions.size());
                throw new IllegalStateException(String.format(
                        "题库不足：难度%s的%s类型题目需要%d道，但只有%d道可用",
                        difficulty.getDescription(), questionType.getDescription(), count, availableQuestions.size()));
            }

            // 随机抽取
            Collections.shuffle(availableQuestions);
            List<Question> selected = availableQuestions.subList(0, count);
            result.addAll(selected);

            // 记录已选择的ID
            selectedIds.addAll(selected.stream().map(Question::getId).collect(Collectors.toList()));
        }

        return result;
    }

    /**
     * 根据难度分布计算每种难度需要抽取的题目数量
     */
    private Map<Difficulty, Integer> calculateCountByDifficulty(
            Integer totalCount, Map<Difficulty, Integer> difficultyDistribution) {

        Map<Difficulty, Integer> result = new HashMap<>();

        if (difficultyDistribution == null || difficultyDistribution.isEmpty()) {
            // 默认平均分配
            int perDifficulty = totalCount / Difficulty.values().length;
            for (Difficulty difficulty : Difficulty.values()) {
                result.put(difficulty, perDifficulty);
            }
        } else {
            // 按比例分配
            int allocated = 0;
            for (int i = 0; i < Difficulty.values().length - 1; i++) {
                Difficulty difficulty = Difficulty.values()[i];
                Integer percentage = difficultyDistribution.getOrDefault(difficulty, 0);
                int count = (int) Math.round(totalCount * percentage / 100.0);
                result.put(difficulty, count);
                allocated += count;
            }
            // 最后一个难度分配剩余的题目（确保总数正确）
            Difficulty last = Difficulty.values()[Difficulty.values().length - 1];
            result.put(last, totalCount - allocated);
        }

        return result;
    }

    /**
     * 根据认证类型和题型获取题目
     */
    private List<Question> getQuestionsByType(String certificationType, QuestionType questionType) {
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        wrapper.eq("type", questionType.getCode());

        if (certificationType != null && !certificationType.isEmpty()) {
            wrapper.and(w -> w.eq("applicable_certification", certificationType)
                    .or().isNull("applicable_certification"));
        } else {
            wrapper.isNull("applicable_certification");
        }

        return questionMapper.selectList(wrapper);
    }

    @Override
    public List<Question> getByCertification(String certificationType) {
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        wrapper.eq("applicable_certification", certificationType);
        return questionMapper.selectList(wrapper);
    }

    @Override
    public List<String> getAllCategories() {
        List<String> categories = questionMapper.findAllCategories();
        // 过滤掉空字符串
        return categories.stream()
                .filter(c -> c != null && !c.trim().isEmpty())
                .sorted()
                .collect(Collectors.toList());
    }
}