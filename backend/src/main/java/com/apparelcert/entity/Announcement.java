package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 系统公告实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("announcement")
public class Announcement extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 公告标题 */
    private String title;
    
    /** 公告内容 */
    private String content;
    
    /** 公告类型：1-系统公告 2-活动通知 3-政策法规 4-行业动态 */
    private Integer type;
    
    /** 状态：0-草稿 1-已发布 2-已下架 */
    private Integer status;
    
    /** 是否置顶 */
    private Integer isTop;
    
    /** 发布时间 */
    private Date publishTime;
    
    /** 发布人ID */
    private Long publisherId;
    
    /** 发布人姓名 */
    private String publisherName;
    
    /** 浏览次数 */
    private Integer views;
    
    /** 封面图片URL */
    private String coverImage;
    
    /** 附件URL */
    private String attachmentUrl;
}
