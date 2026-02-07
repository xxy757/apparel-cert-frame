package com.apparelcert.mapper;

import com.apparelcert.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
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
    List<Question> findByStandardId(@Param("standardId") Long standardId);

    /**
     * 根据认证标准和题型查询题目
     */
    @Select("SELECT * FROM question WHERE (applicable_certification = #{standardId} OR applicable_certification IS NULL) AND type = #{type}")
    List<Question> findByStandardIdAndType(@Param("standardId") Long standardId, @Param("type") String type);

    /**
     * 根据认证标准、题型和难度查询题目
     */
    @Select("SELECT * FROM question WHERE (applicable_certification = #{standardId} OR applicable_certification IS NULL) AND type = #{type} AND difficulty = #{difficulty}")
    List<Question> findByStandardIdAndTypeAndDifficulty(
            @Param("standardId") Long standardId,
            @Param("type") String type,
            @Param("difficulty") String difficulty
    );

    /**
     * 根据认证标准获取所有题目分类
     */
    @Select("SELECT DISTINCT category FROM question WHERE applicable_certification = #{standardId} OR applicable_certification IS NULL")
    List<String> findCategoriesByStandardId(@Param("standardId") Long standardId);

    /**
     * 获取所有题目分类
     */
    @Select("SELECT DISTINCT category FROM question WHERE category IS NOT NULL AND category != ''")
    List<String> findAllCategories();
}