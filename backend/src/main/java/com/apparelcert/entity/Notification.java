package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 消息通知实体类
 */
@Data
@TableName("notification")
public class Notification {

    private Long id;
    private Long userId;
    private Integer userType;
    private String title;
    private String content;
    private Integer isRead;
    private Date createTime;
}
