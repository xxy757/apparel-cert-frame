package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 简历投递实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("resume_delivery")
public class ResumeDelivery extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long positionId;
    private Long resumeId;
    private Integer status;
}
