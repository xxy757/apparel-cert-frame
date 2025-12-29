package com.apparelcert.service;

import com.apparelcert.entity.Announcement;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统公告服务接口
 */
public interface AnnouncementService extends IService<Announcement> {

    /**
     * 发布公告
     * @param announcement 公告信息
     * @return 是否成功
     */
    boolean publishAnnouncement(Announcement announcement);

    /**
     * 保存草稿
     * @param announcement 公告信息
     * @return 是否成功
     */
    boolean saveDraft(Announcement announcement);

    /**
     * 更新公告
     * @param announcement 公告信息
     * @return 是否成功
     */
    boolean updateAnnouncement(Announcement announcement);

    /**
     * 下架公告
     * @param announcementId 公告ID
     * @return 是否成功
     */
    boolean offlineAnnouncement(Long announcementId);

    /**
     * 删除公告
     * @param announcementId 公告ID
     * @return 是否成功
     */
    boolean deleteAnnouncement(Long announcementId);

    /**
     * 获取公告详情
     * @param announcementId 公告ID
     * @return 公告详情
     */
    Announcement getAnnouncementDetail(Long announcementId);

    /**
     * 分页查询公告列表（管理端）
     * @param page 页码
     * @param size 每页数量
     * @param type 类型
     * @param status 状态
     * @param keyword 关键词
     * @return 分页结果
     */
    Page<Announcement> pageQuery(Integer page, Integer size, Integer type, Integer status, String keyword);

    /**
     * 获取已发布的公告列表（前台）
     * @param page 页码
     * @param size 每页数量
     * @param type 类型
     * @return 分页结果
     */
    Page<Announcement> getPublishedAnnouncements(Integer page, Integer size, Integer type);

    /**
     * 获取置顶公告
     * @return 置顶公告列表
     */
    List<Announcement> getTopAnnouncements();

    /**
     * 获取最新公告
     * @param limit 数量限制
     * @return 最新公告列表
     */
    List<Announcement> getLatestAnnouncements(Integer limit);

    /**
     * 增加浏览次数
     * @param announcementId 公告ID
     * @return 是否成功
     */
    boolean increaseViews(Long announcementId);

    /**
     * 设置置顶
     * @param announcementId 公告ID
     * @param isTop 是否置顶
     * @return 是否成功
     */
    boolean setTop(Long announcementId, Integer isTop);
}
