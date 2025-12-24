package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 简历投递实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("application")
public class Application extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long jobId;
    private Long userId;
    private Long enterpriseId;
    private Integer status;
    private String resumeUrl;
    private Date applyTime;
    private String feedback;
    private Date feedbackTime;
}