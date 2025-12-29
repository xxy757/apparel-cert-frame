package com.apparelcert.mapper;

import com.apparelcert.entity.Expert;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评审专家Mapper接口
 */
@Mapper
public interface ExpertMapper extends BaseMapper<Expert> {
}
