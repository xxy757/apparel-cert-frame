package com.apparelcert.entity;

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
}