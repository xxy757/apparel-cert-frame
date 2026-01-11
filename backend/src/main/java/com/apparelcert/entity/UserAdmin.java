package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_admin")
public class UserAdmin extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（唯一）
     */
    private String username;

    /**
     * 密码（BCrypt加密）
     */
    private String password;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 管理员类型：0-超级管理员 1-普通管理员
     */
    private Integer adminType;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 最后登录时间
     */
    private java.util.Date lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;
}
