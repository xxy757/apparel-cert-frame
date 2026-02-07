package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 技能认证实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("certification")
public class Certification extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String certificationType;
    private Integer level;
    private String projectName;
    private String theoryScore;
    private String practicalScore;
    private String reviewComment;
    private Integer status;
    private Date applyTime;
    private Date reviewTime;
    private Long reviewerId;
    private String certificateUrl;
    private String practicalFileUrl;
    private Date expireTime;
}