package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.PracticalTask;
import com.apparelcert.service.PracticalTaskService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实操任务控制器
 */
@RestController
@RequestMapping("/api/admin/certification/practical")
public class PracticalTaskController {

    @Autowired
    private PracticalTaskService taskService;

    /**
     * 分页查询实操任务列表
     */
    @GetMapping
    public Result<Page<PracticalTask>> getTaskList(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer level) {
        Page<PracticalTask> pageInfo = taskService.pageQuery(page, size, keyword, type, level);
        return Result.success(pageInfo);
    }

    /**
     * 获取实操任务详情
     */
    @GetMapping("/detail")
    public Result<PracticalTask> getTaskDetail(@RequestParam Long taskId) {
        PracticalTask task = taskService.getById(taskId);
        return Result.success(task);
    }

    /**
     * 保存或更新实操任务
     */
    @PostMapping
    public Result<Boolean> saveOrUpdateTask(@RequestBody PracticalTask task) {
        boolean result = taskService.saveOrUpdateTask(task);
        return Result.success(result);
    }

    /**
     * 删除实操任务
     */
    @DeleteMapping
    public Result<Boolean> deleteTask(@RequestParam Long taskId) {
        boolean result = taskService.deleteTask(taskId);
        return Result.success(result);
    }

    /**
     * 批量删除实操任务
     */
    @DeleteMapping("/batch")
    public Result<Boolean> deleteBatchTasks(@RequestBody List<Long> ids) {
        boolean result = taskService.deleteBatchTasks(ids);
        return Result.success(result);
    }

    /**
     * 根据认证类型和等级获取实操任务
     */
    @GetMapping("/by-cert-level")
    public Result<PracticalTask> getByCertificationAndLevel(
            @RequestParam String type,
            @RequestParam Integer level) {
        PracticalTask task = taskService.getByCertificationAndLevel(type, level);
        return Result.success(task);
    }

    /**
     * 获取所有实操任务类型
     */
    @GetMapping("/types")
    public Result<List<String>> getAllTypes() {
        List<String> types = taskService.getAllTypes();
        return Result.success(types);
    }

    /**
     * 根据类型获取实操任务列表
     */
    @GetMapping("/by-type")
    public Result<List<PracticalTask>> getByType(@RequestParam String type) {
        List<PracticalTask> tasks = taskService.getByType(type);
        return Result.success(tasks);
    }
}