package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认证标准实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("certification_standard")
public class CertificationStandard extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String name;
    private Integer level;
    private String description;
    private String theoryOutline;
    private String practicalRequirements;
    private String evaluationCriteria;
    private String applicablePositions;
    private String requiredCertificates;
}