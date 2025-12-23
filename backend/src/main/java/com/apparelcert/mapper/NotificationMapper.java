package com.apparelcert.mapper;

import com.apparelcert.entity.Notification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 消息通知Mapper接口
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 批量标记消息为已读
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 更新的行数
     */
    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND user_type = #{userType} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId, @Param("userType") Integer userType);

    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 未读消息数量
     */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND user_type = #{userType} AND is_read = 0")
    int countUnread(@Param("userId") Long userId, @Param("userType") Integer userType);
}

