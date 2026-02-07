package com.apparelcert.enums;

/**
 * 题型枚举
 */
public enum QuestionType {
    /**
     * 单选题
     */
    SINGLE_CHOICE("SINGLE_CHOICE", "单选题", 1),

    /**
     * 多选题
     */
    MULTIPLE_CHOICE("MULTIPLE_CHOICE", "多选题", 2),

    /**
     * 判断题
     */
    TRUE_FALSE("TRUE_FALSE", "判断题", 1),

    /**
     * 简答题
     */
    SHORT_ANSWER("SHORT_ANSWER", "简答题", 2);

    private final String code;
    private final String description;
    private final Integer defaultScore;

    QuestionType(String code, String description, Integer defaultScore) {
        this.code = code;
        this.description = description;
        this.defaultScore = defaultScore;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDefaultScore() {
        return defaultScore;
    }

    /**
     * 根据代码获取题型
     */
    public static QuestionType fromCode(String code) {
        for (QuestionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
