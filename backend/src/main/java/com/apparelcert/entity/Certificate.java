package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 证书实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("certificate")
public class Certificate extends BaseEntity {

    private Long id;
    private Long userId;
    private String certificationType;
    private Integer level;
    private String certificateNumber;
    private String name;
    private String idCard;
    private Date issueDate;
    private Date expireDate;
    private String certificateUrl;
    private String certificateStatus;
    private String issuer;
    private String description;
    private String qrCodeUrl;
}