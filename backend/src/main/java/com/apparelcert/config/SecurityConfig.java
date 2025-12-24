package com.apparelcert.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 为Swagger UI、Knife4j和API路径添加CSRF排除
            .csrf().ignoringAntMatchers(
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/doc.html",
                "/webjars/**",
                "/favicon.ico",
                "/api/**"
            )
            .and()
            // 配置授权规则
            .authorizeRequests()
            // 允许Swagger UI和Knife4j相关路径的访问
            .antMatchers(
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/doc.html",
                "/webjars/**",
                "/favicon.ico"
            ).permitAll()
            // 允许认证相关路径（登录、注册）
            .antMatchers("/api/auth/**").permitAll()
            // 其他路径需要认证
            .anyRequest().authenticated()
            .and()
            // 启用基本认证
            .httpBasic();
    }
}