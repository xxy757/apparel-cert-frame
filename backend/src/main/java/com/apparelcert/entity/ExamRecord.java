package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 考试记录实体类
 */
@Data
@TableName("exam_record")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 认证申请ID
     */
    private Long applicationId;
    
    /**
     * 认证标准ID
     */
    private Long standardId;
    
    /**
     * 试卷内容（JSON格式，包含题目ID列表）
     */
    private String paperContent;
    
    /**
     * 用户答案（JSON格式）
     */
    private String userAnswers;
    
    /**
     * 得分
     */
    private Integer score;
    
    /**
     * 总分
     */
    private Integer totalScore;
    
    /**
     * 考试开始时间
     */
    private Date startTime;
    
    /**
     * 考试结束时间
     */
    private Date endTime;
    
    /**
     * 考试时长（分钟）
     */
    private Integer duration;
    
    /**
     * 状态 0:进行中 1:已完成 2:超时
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}
