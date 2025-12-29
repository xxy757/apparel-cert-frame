package com.apparelcert.mapper;

import com.apparelcert.entity.Announcement;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统公告Mapper接口
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
