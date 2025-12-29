package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 定时投递实体类
 */
@Data
@TableName("scheduled_delivery")
public class ScheduledDelivery {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 岗位ID */
    private Long jobId;
    
    /** 简历ID */
    private Long resumeId;
    
    /** 计划投递时间 */
    private Date scheduledTime;
    
    /** 状态：0-待执行，1-已执行，2-已取消，3-执行失败 */
    private Integer status;
    
    /** 执行时间 */
    private Date executedTime;
    
    /** 执行结果说明 */
    private String resultMessage;
    
    private Date createTime;
    
    private Date updateTime;
}
