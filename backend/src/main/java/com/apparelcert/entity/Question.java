package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

/**
 * 题目实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question")
public class Question extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String content;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private String explanation;
    private String difficulty;
    private String category;
    private Integer score;
    private String applicableCertification;
    
    /**
     * 获取选项Map
     */
    public Map<String, String> getOptions() {
        Map<String, String> options = new HashMap<>();
        if (optionA != null) options.put("A", optionA);
        if (optionB != null) options.put("B", optionB);
        if (optionC != null) options.put("C", optionC);
        if (optionD != null) options.put("D", optionD);
        return options;
    }
    
    /**
     * 获取正确答案（别名方法）
     */
    public String getAnswer() {
        return correctAnswer;
    }
}