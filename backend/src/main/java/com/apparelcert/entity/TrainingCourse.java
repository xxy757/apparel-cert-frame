package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 培训课程实体类
 */
@Data
@TableName("training_course")
public class TrainingCourse {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 课程标题 */
    private String title;
    
    /** 课程描述 */
    private String description;
    
    /** 封面图片 */
    private String coverImage;
    
    /** 课程类型 */
    private String courseType;
    
    /** 适合岗位类型 */
    private String suitableJobType;
    
    /** 适合等级 */
    private String suitableLevel;
    
    /** 价格 */
    private BigDecimal price;
    
    /** 课程提供方 */
    private String provider;
    
    /** 课程链接 */
    private String url;
    
    /** 课程时长（分钟） */
    private Integer duration;
    
    /** 浏览次数 */
    private Integer viewCount;
    
    /** 收藏次数 */
    private Integer collectCount;
    
    /** 状态：0-禁用，1-启用 */
    private Integer status;
    
    private Date createTime;
    
    private Date updateTime;
}
