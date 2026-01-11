package com.apparelcert.mapper;

import com.apparelcert.entity.UserAdmin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 管理员用户Mapper接口
 */
@Mapper
public interface UserAdminMapper extends BaseMapper<UserAdmin> {

    /**
     * 根据用户名查询管理员
     * @param username 用户名
     * @return 管理员信息
     */
    @Select("SELECT * FROM user_admin WHERE username = #{username} AND status = 1")
    UserAdmin findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询管理员
     * @param email 邮箱
     * @return 管理员信息
     */
    @Select("SELECT * FROM user_admin WHERE email = #{email} AND status = 1")
    UserAdmin findByEmail(@Param("email") String email);

    /**
     * 更新最后登录信息
     * @param id 管理员ID
     * @param lastLoginIp 最后登录IP
     * @return 更新结果
     */
    @Update("UPDATE user_admin SET last_login_time = NOW(), last_login_ip = #{lastLoginIp} WHERE id = #{id}")
    int updateLastLoginInfo(@Param("id") Long id, @Param("lastLoginIp") String lastLoginIp);
}
