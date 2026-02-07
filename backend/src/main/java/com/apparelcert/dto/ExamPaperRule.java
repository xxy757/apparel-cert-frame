package com.apparelcert.dto;

import com.apparelcert.enums.Difficulty;
import com.apparelcert.enums.QuestionType;
import lombok.Data;

import java.util.Map;

/**
 * 组卷规则配置
 */
@Data
public class ExamPaperRule {

    /**
     * 认证标准ID
     */
    private Long standardId;

    /**
     * 认证类型（如：designer、patternmaker等）
     */
    private String certificationType;

    /**
     * 等级（1=初级, 2=中级, 3=高级）
     */
    private Integer level;

    /**
     * 题目总数
     */
    private Integer totalQuestionCount;

    /**
     * 题型分布：题型 -> 题目数量
     * 例如：{"SINGLE_CHOICE": 10, "MULTIPLE_CHOICE": 5, "TRUE_FALSE": 5}
     */
    private Map<QuestionType, Integer> typeDistribution;

    /**
     * 难度分布：难度 -> 占比（百分比，0-100）
     * 例如：{"EASY": 30, "MEDIUM": 50, "HARD": 20}
     */
    private Map<Difficulty, Integer> difficultyDistribution;

    /**
     * 是否允许题目重复（同一次考试中）
     */
    private boolean allowDuplicate = false;

    /**
     * 是否强制覆盖所有知识点
     */
    private boolean requireCategoryCoverage = false;

    /**
     * 默认组卷规则 - 初级认证
     */
    public static ExamPaperRule createDefaultRule(Long standardId, String certificationType, Integer level) {
        ExamPaperRule rule = new ExamPaperRule();
        rule.setStandardId(standardId);
        rule.setCertificationType(certificationType);
        rule.setLevel(level);
        rule.setTotalQuestionCount(20);
        rule.setAllowDuplicate(false);

        // 根据等级设置难度分布
        if (level == 1) {
            // 初级：简单40%、中等50%、困难10%
            rule.setDifficultyDistribution(Map.of(
                Difficulty.EASY, 40,
                Difficulty.MEDIUM, 50,
                Difficulty.HARD, 10
            ));
        } else if (level == 2) {
            // 中级：简单30%、中等50%、困难20%
            rule.setDifficultyDistribution(Map.of(
                Difficulty.EASY, 30,
                Difficulty.MEDIUM, 50,
                Difficulty.HARD, 20
            ));
        } else {
            // 高级：简单20%、中等50%、困难30%
            rule.setDifficultyDistribution(Map.of(
                Difficulty.EASY, 20,
                Difficulty.MEDIUM, 50,
                Difficulty.HARD, 30
            ));
        }

        // 题型分布：单选题10、多选题5、判断题5
        rule.setTypeDistribution(Map.of(
            QuestionType.SINGLE_CHOICE, 10,
            QuestionType.MULTIPLE_CHOICE, 5,
            QuestionType.TRUE_FALSE, 5
        ));

        return rule;
    }

    /**
     * 获取指定题型的题目数量
     */
    public Integer getCountByType(QuestionType type) {
        return typeDistribution != null ? typeDistribution.getOrDefault(type, 0) : 0;
    }

    /**
     * 获取指定难度的占比
     */
    public Integer getPercentageByDifficulty(Difficulty difficulty) {
        return difficultyDistribution != null ? difficultyDistribution.getOrDefault(difficulty, 0) : 0;
    }
}
