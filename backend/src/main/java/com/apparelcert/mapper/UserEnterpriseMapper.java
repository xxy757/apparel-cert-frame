package com.apparelcert.mapper;

import com.apparelcert.entity.UserEnterprise;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 企业用户Mapper接口
 */
@Mapper
public interface UserEnterpriseMapper extends BaseMapper<UserEnterprise> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM user_enterprise WHERE username = #{username} AND status = 1")
    UserEnterprise findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM user_enterprise WHERE email = #{email} AND status = 1")
    UserEnterprise findByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM user_enterprise WHERE contact_phone = #{phone} AND status = 1")
    UserEnterprise findByPhone(@Param("phone") String phone);
}