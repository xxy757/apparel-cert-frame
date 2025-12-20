package com.apparelcert.entity;

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

    private Long id;
    private Long enterpriseId;
    private String title;
    private String type;
    private String level;
    private String salary;
    private String location;
    private String education;
    private Integer experience;
    private String description;
    private String requirements;
    private String benefits;
    private Integer status;
    private Integer views;
    private Integer applications;
    private String contactPerson;
    private String contactPhone;
}