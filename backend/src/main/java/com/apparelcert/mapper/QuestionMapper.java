package com.apparelcert.mapper;

import com.apparelcert.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 题目Mapper接口
 */
public interface QuestionMapper extends BaseMapper<Question> {
    
    /**
     * 根据认证标准ID查询题目
     */
    @Select("SELECT * FROM question WHERE applicable_certification = #{standardId} OR applicable_certification IS NULL")
    List<Question> findByStandardId(Long standardId);
}