package com.apparelcert.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("job")
public class Job extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long enterpriseId;
    private String title;
    @JsonAlias("jobType")
    private String type;
    private String level;
    private String salary;
    private Integer salaryMin;
    private Integer salaryMax;
    @JsonAlias("workLocation")
    private String location;
    private String workType;
    private Integer recruitNum;
    private String skills;
    private String certificationRequirement;
    private Integer isUrgent;
    private String education;
    private Integer experience;
    private String description;
    private String requirements;
    private String benefits;
    private Integer status;
    @JsonAlias("viewCount")
    private Integer views;
    @JsonAlias("applyCount")
    private Integer applications;
    private String contactPerson;
    private String contactPhone;

    @TableField(exist = false)
    private String companyName;

    @TableField(exist = false)
    private String companyLogo;
}
