package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 登录尝试记录实体类
 * 用于记录登录失败次数和账号锁定状态
 */
@Data
@TableName("login_attempt")
public class LoginAttempt {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户类型 1:个人用户 2:企业用户
     */
    private Integer userType;
    
    /**
     * 失败次数
     */
    private Integer failCount;
    
    /**
     * 锁定时间
     */
    private Date lockTime;
    
    /**
     * 最后尝试时间
     */
    private Date lastAttemptTime;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}
