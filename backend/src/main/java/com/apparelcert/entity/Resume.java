package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 个人简历实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("resume")
public class Resume extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String email;
    private String gender;
    private Integer age;
    private String education;
    private String major;
    private String school;
    private String careerDirection;
    private String workExperience;
    private String projectExperience;
    private String skills;
    private String certificates;
    private String selfIntroduction;
    private Integer isPublic;
    private String pdfUrl;
}