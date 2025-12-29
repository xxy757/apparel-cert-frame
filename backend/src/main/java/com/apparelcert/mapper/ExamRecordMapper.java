package com.apparelcert.mapper;

import com.apparelcert.entity.ExamRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 考试记录Mapper接口
 */
@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {

    /**
     * 根据用户ID和认证申请ID查询考试记录
     */
    @Select("SELECT * FROM exam_record WHERE user_id = #{userId} AND application_id = #{applicationId} ORDER BY create_time DESC LIMIT 1")
    ExamRecord findByUserAndApplication(@Param("userId") Long userId, @Param("applicationId") Long applicationId);

    /**
     * 查询用户的所有考试记录
     */
    @Select("SELECT * FROM exam_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ExamRecord> findByUserId(@Param("userId") Long userId);
}
