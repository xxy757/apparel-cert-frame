package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 面试实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("interview")
public class Interview extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 投递ID */
    private Long deliveryId;
    
    /** 用户ID */
    private Long userId;
    
    /** 企业ID */
    private Long enterpriseId;
    
    /** 岗位ID */
    private Long jobId;
    
    /** 面试时间 */
    private Date interviewTime;
    
    /** 面试地点 */
    private String interviewLocation;
    
    /** 面试类型：现场/视频/电话 */
    private String interviewType;
    
    /** 面试官 */
    private String interviewer;
    
    /** 面试反馈 */
    private String feedback;
    
    /** 面试结果：0-未通过 1-通过 */
    private Integer result;
    
    /** 面试状态：0-待确认 1-已确认 2-已面试 3-已取消 */
    private Integer status;
    
    /** 面试评价（1-5分） */
    private Integer evaluation;
    
    /** 候选人优势 */
    private String strengths;
    
    /** 候选人不足 */
    private String weaknesses;
    
    /** 录用状态：0-待定 1-录用 2-不录用 */
    private Integer hireStatus;
    
    /** 薪资 */
    private String salary;
    
    /** 入职日期 */
    private String entryDate;
    
    /** 备注 */
    private String remark;
}
