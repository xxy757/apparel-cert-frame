package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_enterprise")
public class UserEnterprise extends BaseEntity {

    private Long id;
    private String username;
    private String password;
    private String companyName;
    private String companyType;
    private String contactPerson;
    private String contactPhone;
    private String email;
    private String businessLicense;
    private String address;
    private String description;
    private String logo;
    private Integer authStatus;
}
