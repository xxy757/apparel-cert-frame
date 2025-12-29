package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 个人简历实体类
 */
@Data
@TableName("resume")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 基本信息（JSON格式）
     */
    @TableField("basic_info")
    private String basicInfo;
    
    /**
     * 教育背景（JSON格式数组）
     */
    private String education;
    
    /**
     * 工作经历（JSON格式数组）
     */
    @TableField("work_experience")
    private String workExperience;
    
    /**
     * 项目经验（JSON格式数组）
     */
    @TableField("project_experience")
    private String projectExperience;
    
    /**
     * 技能（JSON格式数组）
     */
    private String skills;
    
    /**
     * 证书（JSON格式数组）
     */
    private String certificates;
    
    /**
     * 是否公开：0-私有，1-公开
     */
    @TableField("is_public")
    private Integer isPublic;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
