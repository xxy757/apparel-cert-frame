package com.apparelcert.service.impl;

import com.apparelcert.entity.Announcement;
import com.apparelcert.mapper.AnnouncementMapper;
import com.apparelcert.service.AnnouncementService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 系统公告服务实现类
 */
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> 
        implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public boolean publishAnnouncement(Announcement announcement) {
        announcement.setStatus(1); // 已发布
        announcement.setPublishTime(new Date());
        announcement.setViews(0);
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(0);
        }
        return this.save(announcement);
    }

    @Override
    public boolean saveDraft(Announcement announcement) {
        announcement.setStatus(0); // 草稿
        announcement.setViews(0);
        return this.save(announcement);
    }

    @Override
    public boolean updateAnnouncement(Announcement announcement) {
        return this.updateById(announcement);
    }

    @Override
    public boolean offlineAnnouncement(Long announcementId) {
        Announcement announcement = new Announcement();
        announcement.setId(announcementId);
        announcement.setStatus(2); // 已下架
        return this.updateById(announcement);
    }

    @Override
    public boolean deleteAnnouncement(Long announcementId) {
        return this.removeById(announcementId);
    }

    @Override
    public Announcement getAnnouncementDetail(Long announcementId) {
        return announcementMapper.selectById(announcementId);
    }

    @Override
    public Page<Announcement> pageQuery(Integer page, Integer size, Integer type, Integer status, String keyword) {
        Page<Announcement> pageInfo = new Page<>(page, size);
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        
        if (type != null) {
            wrapper.eq("type", type);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("title", keyword).or().like("content", keyword));
        }
        
        wrapper.orderByDesc("is_top");
        wrapper.orderByDesc("publish_time");
        return announcementMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public Page<Announcement> getPublishedAnnouncements(Integer page, Integer size, Integer type) {
        Page<Announcement> pageInfo = new Page<>(page, size);
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 已发布
        
        if (type != null) {
            wrapper.eq("type", type);
        }
        
        wrapper.orderByDesc("is_top");
        wrapper.orderByDesc("publish_time");
        return announcementMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public List<Announcement> getTopAnnouncements() {
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.eq("is_top", 1);
        wrapper.orderByDesc("publish_time");
        return announcementMapper.selectList(wrapper);
    }

    @Override
    public List<Announcement> getLatestAnnouncements(Integer limit) {
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByDesc("publish_time");
        wrapper.last("limit " + limit);
        return announcementMapper.selectList(wrapper);
    }

    @Override
    public boolean increaseViews(Long announcementId) {
        Announcement announcement = this.getById(announcementId);
        if (announcement != null) {
            announcement.setViews((announcement.getViews() != null ? announcement.getViews() : 0) + 1);
            return this.updateById(announcement);
        }
        return false;
    }

    @Override
    public boolean setTop(Long announcementId, Integer isTop) {
        Announcement announcement = new Announcement();
        announcement.setId(announcementId);
        announcement.setIsTop(isTop);
        return this.updateById(announcement);
    }
}
