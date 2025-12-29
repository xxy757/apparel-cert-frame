package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 专家评审实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("expert_review")
public class ExpertReview extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 认证ID */
    private Long certificationId;
    
    /** 专家ID */
    private Long expertId;
    
    /** 专家姓名 */
    private String expertName;
    
    /** 评审类型：1-理论考试 2-实操评审 */
    private Integer reviewType;
    
    /** 评审分数 */
    private Integer score;
    
    /** 评审等级：A/B/C/D */
    private String grade;
    
    /** 评审意见 */
    private String comment;
    
    /** 评审状态：0-待评审 1-已评审 2-已驳回 */
    private Integer status;
    
    /** 评审时间 */
    private Date reviewTime;
    
    /** 分配时间 */
    private Date assignTime;
    
    /** 附件URL（多个用逗号分隔） */
    private String attachments;
}
