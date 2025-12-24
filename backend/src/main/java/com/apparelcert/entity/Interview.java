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
    private Long deliveryId;
    private Date interviewTime;
    private String interviewLocation;
    private String interviewType;
    private String interviewer;
    private String feedback;
    private Integer result;
}
