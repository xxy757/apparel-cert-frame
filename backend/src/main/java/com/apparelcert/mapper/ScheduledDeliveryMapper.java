package com.apparelcert.mapper;

import com.apparelcert.entity.ScheduledDelivery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时投递Mapper
 */
@Mapper
public interface ScheduledDeliveryMapper extends BaseMapper<ScheduledDelivery> {
}
