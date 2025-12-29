package com.apparelcert.service;

import com.apparelcert.entity.TrainingCourse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 培训课程服务接口
 */
public interface TrainingCourseService extends IService<TrainingCourse> {
    
    /**
     * 分页查询课程
     */
    Page<TrainingCourse> pageQuery(Integer page, Integer size, String keyword, 
                                    String courseType, String suitableJobType, String suitableLevel);
    
    /**
     * 获取推荐课程（基于用户认证进度）
     */
    List<TrainingCourse> getRecommendedCourses(Long userId, Integer limit);
    
    /**
     * 增加浏览次数
     */
    void increaseViewCount(Long courseId);
    
    /**
     * 收藏课程
     */
    boolean collectCourse(Long userId, Long courseId);
    
    /**
     * 取消收藏
     */
    boolean uncollectCourse(Long userId, Long courseId);
    
    /**
     * 检查是否已收藏
     */
    boolean isCollected(Long userId, Long courseId);
    
    /**
     * 获取用户收藏的课程
     */
    Page<TrainingCourse> getCollectedCourses(Long userId, Integer page, Integer size);
}
