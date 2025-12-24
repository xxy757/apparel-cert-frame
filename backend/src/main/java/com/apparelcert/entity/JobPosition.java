package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 职位实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("job_position")
public class JobPosition extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long enterpriseId;
    private String title;
    private String jobType;
    private String certificationRequirement;
    private Double salaryMin;
    private Double salaryMax;
    private String workLocation;
    private String workType;
    private String description;
    private String requirements;
    private String benefits;
    private Integer viewCount;
    private Integer applyCount;
}
