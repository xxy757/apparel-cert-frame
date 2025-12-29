package com.apparelcert.mapper;

import com.apparelcert.entity.LoginAttempt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 登录尝试记录Mapper接口
 */
@Mapper
public interface LoginAttemptMapper extends BaseMapper<LoginAttempt> {

    /**
     * 根据用户名和用户类型查询登录尝试记录
     */
    @Select("SELECT * FROM login_attempt WHERE username = #{username} AND user_type = #{userType}")
    LoginAttempt findByUsernameAndType(@Param("username") String username, @Param("userType") Integer userType);
}
