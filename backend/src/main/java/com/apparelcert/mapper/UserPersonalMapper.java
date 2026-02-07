package com.apparelcert.mapper;

import com.apparelcert.entity.UserPersonal;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 个人用户Mapper接口
 */
@Mapper
public interface UserPersonalMapper extends BaseMapper<UserPersonal> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM user_personal WHERE username = #{username} AND status = 1")
    UserPersonal findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM user_personal WHERE email = #{email} AND status = 1")
    UserPersonal findByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM user_personal WHERE phone = #{phone} AND status = 1")
    UserPersonal findByPhone(@Param("phone") String phone);
}

