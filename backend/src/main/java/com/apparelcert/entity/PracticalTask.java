package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 实操任务实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("practical_task")
public class PracticalTask extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String requirements;
    private String submissionFormat;
    private String evaluationCriteria;
    private String applicableCertification;
    private String type;
    private Integer level;
    private Integer maxScore;
    private String requiredTools;
    private String exampleUrl;

    @TableField(exist = false)
    private Integer status;
}
