package com.apparelcert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 课程收藏实体类
 */
@Data
@TableName("course_collection")
public class CourseCollection {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 课程ID */
    private Long courseId;
    
    /** 收藏时间 */
    private Date createTime;
}
