package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.TrainingCourse;
import com.apparelcert.service.TrainingCourseService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 培训课程控制器
 */
@RestController
@RequestMapping("/api/course")
public class TrainingCourseController {

    @Autowired
    private TrainingCourseService courseService;

    /**
     * 分页查询课程列表
     */
    @GetMapping("/list")
    public Result<Page<TrainingCourse>> getCourseList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String courseType,
            @RequestParam(required = false) String suitableJobType,
            @RequestParam(required = false) String suitableLevel) {
        Page<TrainingCourse> pageInfo = courseService.pageQuery(page, size, keyword, 
                courseType, suitableJobType, suitableLevel);
        return Result.success(pageInfo);
    }

    /**
     * 获取课程详情
     */
    @GetMapping("/detail")
    public Result<TrainingCourse> getCourseDetail(@RequestParam Long courseId) {
        courseService.increaseViewCount(courseId);
        TrainingCourse course = courseService.getById(courseId);
        return Result.success(course);
    }

    /**
     * 获取推荐课程
     */
    @GetMapping("/recommend")
    public Result<List<TrainingCourse>> getRecommendedCourses(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "6") Integer limit) {
        List<TrainingCourse> courses = courseService.getRecommendedCourses(userId, limit);
        return Result.success(courses);
    }

    /**
     * 收藏课程
     */
    @PostMapping("/collect")
    public Result<Boolean> collectCourse(@RequestParam Long userId, @RequestParam Long courseId) {
        boolean result = courseService.collectCourse(userId, courseId);
        return result ? Result.success(true) : Result.error(500, "收藏失败");
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/collect")
    public Result<Boolean> uncollectCourse(@RequestParam Long userId, @RequestParam Long courseId) {
        boolean result = courseService.uncollectCourse(userId, courseId);
        return result ? Result.success(true) : Result.error(500, "取消收藏失败");
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/collect/check")
    public Result<Map<String, Object>> checkCollected(@RequestParam Long userId, @RequestParam Long courseId) {
        Map<String, Object> result = new HashMap<>();
        result.put("collected", courseService.isCollected(userId, courseId));
        return Result.success(result);
    }

    /**
     * 获取用户收藏的课程
     */
    @GetMapping("/collect/list")
    public Result<Page<TrainingCourse>> getCollectedCourses(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<TrainingCourse> pageInfo = courseService.getCollectedCourses(userId, page, size);
        return Result.success(pageInfo);
    }

    // ========== 管理端接口 ==========

    /**
     * 添加课程（管理端）
     */
    @PostMapping("/admin/add")
    public Result<Boolean> addCourse(@RequestBody TrainingCourse course) {
        course.setViewCount(0);
        course.setCollectCount(0);
        course.setStatus(1);
        boolean result = courseService.save(course);
        return result ? Result.success(true) : Result.error(500, "添加失败");
    }

    /**
     * 更新课程（管理端）
     */
    @PutMapping("/admin/update")
    public Result<Boolean> updateCourse(@RequestBody TrainingCourse course) {
        boolean result = courseService.updateById(course);
        return result ? Result.success(true) : Result.error(500, "更新失败");
    }

    /**
     * 删除课程（管理端）
     */
    @DeleteMapping("/admin/delete")
    public Result<Boolean> deleteCourse(@RequestParam Long courseId) {
        boolean result = courseService.removeById(courseId);
        return result ? Result.success(true) : Result.error(500, "删除失败");
    }

    /**
     * 更新课程状态（管理端）
     */
    @PutMapping("/admin/status")
    public Result<Boolean> updateCourseStatus(@RequestParam Long courseId, @RequestParam Integer status) {
        TrainingCourse course = new TrainingCourse();
        course.setId(courseId);
        course.setStatus(status);
        boolean result = courseService.updateById(course);
        return result ? Result.success(true) : Result.error(500, "更新失败");
    }
}
