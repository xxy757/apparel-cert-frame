package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评审专家实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("expert")
public class Expert extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID（关联user_personal表） */
    private Long userId;
    
    /** 专家姓名 */
    private String name;
    
    /** 专家编号 */
    private String expertCode;
    
    /** 专业领域（多个用逗号分隔） */
    private String specialties;
    
    /** 职称 */
    private String title;
    
    /** 工作单位 */
    private String organization;
    
    /** 联系电话 */
    private String phone;
    
    /** 邮箱 */
    private String email;
    
    /** 简介 */
    private String introduction;
    
    /** 状态：0-禁用 1-启用 */
    private Integer status;
    
    /** 评审等级：1-初级评审 2-中级评审 3-高级评审 */
    private Integer reviewLevel;
    
    /** 累计评审数 */
    private Integer totalReviews;
    
    /** 头像URL */
    private String avatar;
}
