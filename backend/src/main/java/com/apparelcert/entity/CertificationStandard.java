package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    @TableField("job_type")
    private String type;
    private String name;
    private Integer level;
    private String description;
    @TableField("theory_requirements")
    private String theoryOutline;
    @TableField("practical_requirements")
    private String practicalRequirements;
    @TableField(exist = false)
    private String evaluationCriteria;
    @TableField(exist = false)
    private String applicablePositions;
    @TableField(exist = false)
    private String requiredCertificates;
}
