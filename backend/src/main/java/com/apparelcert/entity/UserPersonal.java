package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 个人用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_personal")
public class UserPersonal extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String name;
    private String phone;
    private String email;
    private String idCard;
    private String careerDirection;
    private String avatar;
    private Integer resumeStatus;
}
