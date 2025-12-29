package com.apparelcert.service.impl;

import com.apparelcert.entity.CourseCollection;
import com.apparelcert.entity.TrainingCourse;
import com.apparelcert.mapper.CourseCollectionMapper;
import com.apparelcert.mapper.TrainingCourseMapper;
import com.apparelcert.service.TrainingCourseService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 培训课程服务实现类
 */
@Service
public class TrainingCourseServiceImpl extends ServiceImpl<TrainingCourseMapper, TrainingCourse> 
        implements TrainingCourseService {

    @Autowired
    private TrainingCourseMapper courseMapper;
    
    @Autowired
    private CourseCollectionMapper collectionMapper;

    @Override
    public Page<TrainingCourse> pageQuery(Integer page, Integer size, String keyword,
                                          String courseType, String suitableJobType, String suitableLevel) {
        Page<TrainingCourse> pageInfo = new Page<>(page, size);
        QueryWrapper<TrainingCourse> wrapper = new QueryWrapper<>();
        
        wrapper.eq("status", 1);
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("title", keyword)
                .or().like("description", keyword)
                .or().like("provider", keyword));
        }
        if (courseType != null && !courseType.isEmpty()) {
            wrapper.eq("course_type", courseType);
        }
        if (suitableJobType != null && !suitableJobType.isEmpty()) {
            wrapper.eq("suitable_job_type", suitableJobType);
        }
        if (suitableLevel != null && !suitableLevel.isEmpty()) {
            wrapper.eq("suitable_level", suitableLevel);
        }
        
        wrapper.orderByDesc("collect_count", "view_count", "create_time");
        return courseMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public List<TrainingCourse> getRecommendedCourses(Long userId, Integer limit) {
        // 简化实现：返回热门课程
        // 实际项目中应该基于用户的认证进度和偏好进行推荐
        QueryWrapper<TrainingCourse> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByDesc("collect_count", "view_count");
        wrapper.last("LIMIT " + limit);
        return courseMapper.selectList(wrapper);
    }

    @Override
    public void increaseViewCount(Long courseId) {
        TrainingCourse course = this.getById(courseId);
        if (course != null) {
            course.setViewCount((course.getViewCount() != null ? course.getViewCount() : 0) + 1);
            this.updateById(course);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean collectCourse(Long userId, Long courseId) {
        // 检查是否已收藏
        if (isCollected(userId, courseId)) {
            return true;
        }
        
        // 添加收藏记录
        CourseCollection collection = new CourseCollection();
        collection.setUserId(userId);
        collection.setCourseId(courseId);
        collection.setCreateTime(new Date());
        int result = collectionMapper.insert(collection);
        
        // 更新课程收藏数
        if (result > 0) {
            TrainingCourse course = this.getById(courseId);
            if (course != null) {
                course.setCollectCount((course.getCollectCount() != null ? course.getCollectCount() : 0) + 1);
                this.updateById(course);
            }
        }
        
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean uncollectCourse(Long userId, Long courseId) {
        QueryWrapper<CourseCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("course_id", courseId);
        int result = collectionMapper.delete(wrapper);
        
        // 更新课程收藏数
        if (result > 0) {
            TrainingCourse course = this.getById(courseId);
            if (course != null && course.getCollectCount() != null && course.getCollectCount() > 0) {
                course.setCollectCount(course.getCollectCount() - 1);
                this.updateById(course);
            }
        }
        
        return result > 0;
    }

    @Override
    public boolean isCollected(Long userId, Long courseId) {
        QueryWrapper<CourseCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("course_id", courseId);
        return collectionMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Page<TrainingCourse> getCollectedCourses(Long userId, Integer page, Integer size) {
        // 获取用户收藏的课程ID
        QueryWrapper<CourseCollection> collectionWrapper = new QueryWrapper<>();
        collectionWrapper.eq("user_id", userId);
        collectionWrapper.orderByDesc("create_time");
        List<CourseCollection> collections = collectionMapper.selectList(collectionWrapper);
        
        if (collections.isEmpty()) {
            return new Page<>(page, size);
        }
        
        List<Long> courseIds = collections.stream()
            .map(CourseCollection::getCourseId)
            .collect(Collectors.toList());
        
        // 分页查询课程
        Page<TrainingCourse> pageInfo = new Page<>(page, size);
        QueryWrapper<TrainingCourse> courseWrapper = new QueryWrapper<>();
        courseWrapper.in("id", courseIds);
        courseWrapper.eq("status", 1);
        
        return courseMapper.selectPage(pageInfo, courseWrapper);
    }
}
