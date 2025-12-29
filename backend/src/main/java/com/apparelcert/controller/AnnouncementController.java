package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.Announcement;
import com.apparelcert.service.AnnouncementService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统公告控制器
 */
@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 发布公告（管理端）
     */
    @PostMapping("/publish")
    public Result<Boolean> publishAnnouncement(@RequestBody Announcement announcement) {
        boolean result = announcementService.publishAnnouncement(announcement);
        return result ? Result.success(true) : Result.error(500, "发布失败");
    }

    /**
     * 保存草稿（管理端）
     */
    @PostMapping("/draft")
    public Result<Boolean> saveDraft(@RequestBody Announcement announcement) {
        boolean result = announcementService.saveDraft(announcement);
        return result ? Result.success(true) : Result.error(500, "保存失败");
    }

    /**
     * 更新公告（管理端）
     */
    @PutMapping
    public Result<Boolean> updateAnnouncement(@RequestBody Announcement announcement) {
        boolean result = announcementService.updateAnnouncement(announcement);
        return result ? Result.success(true) : Result.error(500, "更新失败");
    }

    /**
     * 下架公告（管理端）
     */
    @PutMapping("/offline")
    public Result<Boolean> offlineAnnouncement(@RequestParam Long announcementId) {
        boolean result = announcementService.offlineAnnouncement(announcementId);
        return result ? Result.success(true) : Result.error(500, "下架失败");
    }

    /**
     * 删除公告（管理端）
     */
    @DeleteMapping
    public Result<Boolean> deleteAnnouncement(@RequestParam Long announcementId) {
        boolean result = announcementService.deleteAnnouncement(announcementId);
        return result ? Result.success(true) : Result.error(500, "删除失败");
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/detail")
    public Result<Announcement> getAnnouncementDetail(@RequestParam Long announcementId) {
        // 增加浏览次数
        announcementService.increaseViews(announcementId);
        Announcement announcement = announcementService.getAnnouncementDetail(announcementId);
        return Result.success(announcement);
    }

    /**
     * 分页查询公告列表（管理端）
     */
    @GetMapping("/admin/list")
    public Result<Page<Announcement>> pageQuery(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Page<Announcement> pageInfo = announcementService.pageQuery(page, size, type, status, keyword);
        return Result.success(pageInfo);
    }

    /**
     * 获取已发布的公告列表（前台）
     */
    @GetMapping("/list")
    public Result<Page<Announcement>> getPublishedAnnouncements(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type) {
        Page<Announcement> pageInfo = announcementService.getPublishedAnnouncements(page, size, type);
        return Result.success(pageInfo);
    }

    /**
     * 获取置顶公告
     */
    @GetMapping("/top")
    public Result<List<Announcement>> getTopAnnouncements() {
        List<Announcement> announcements = announcementService.getTopAnnouncements();
        return Result.success(announcements);
    }

    /**
     * 获取最新公告
     */
    @GetMapping("/latest")
    public Result<List<Announcement>> getLatestAnnouncements(
            @RequestParam(defaultValue = "5") Integer limit) {
        List<Announcement> announcements = announcementService.getLatestAnnouncements(limit);
        return Result.success(announcements);
    }

    /**
     * 设置置顶（管理端）
     */
    @PutMapping("/top")
    public Result<Boolean> setTop(
            @RequestParam Long announcementId,
            @RequestParam Integer isTop) {
        boolean result = announcementService.setTop(announcementId, isTop);
        return result ? Result.success(true) : Result.error(500, "设置失败");
    }
    
    /**
     * 发布行业动态
     */
    @PostMapping("/industry-news")
    public Result<Boolean> publishIndustryNews(@RequestBody Announcement announcement) {
        announcement.setType(4); // 4-行业动态
        boolean result = announcementService.publishAnnouncement(announcement);
        return result ? Result.success(true) : Result.error(500, "发布失败");
    }
    
    /**
     * 获取行业动态列表
     */
    @GetMapping("/industry-news")
    public Result<Page<Announcement>> getIndustryNews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Announcement> pageInfo = announcementService.getPublishedAnnouncements(page, size, 4);
        return Result.success(pageInfo);
    }
}
