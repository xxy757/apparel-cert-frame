package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 简历收藏实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("resume_collection")
public class ResumeCollection extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 企业ID */
    private Long enterpriseId;
    
    /** 简历用户ID */
    private Long resumeUserId;
    
    /** 意向程度：1-高 2-中 3-低 */
    private Integer intentionLevel;
    
    /** 备注 */
    private String notes;
    
    /** 标签（多个用逗号分隔） */
    private String tags;
    
    /** 收藏时间 */
    private Date collectTime;
}
