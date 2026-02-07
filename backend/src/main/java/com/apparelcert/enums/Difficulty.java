package com.apparelcert.enums;

/**
 * 题目难度枚举
 */
public enum Difficulty {
    /**
     * 简单
     */
    EASY("EASY", "简单", 1),

    /**
     * 中等
     */
    MEDIUM("MEDIUM", "中等", 2),

    /**
     * 困难
     */
    HARD("HARD", "困难", 3);

    private final String code;
    private final String description;
    private final Integer level;

    Difficulty(String code, String description, Integer level) {
        this.code = code;
        this.description = description;
        this.level = level;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Integer getLevel() {
        return level;
    }

    /**
     * 根据代码获取难度
     */
    public static Difficulty fromCode(String code) {
        for (Difficulty difficulty : values()) {
            if (difficulty.code.equals(code)) {
                return difficulty;
            }
        }
        return null;
    }
}
