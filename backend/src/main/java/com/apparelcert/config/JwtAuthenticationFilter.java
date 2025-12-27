package com.apparelcert.config;

import com.apparelcert.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 从请求头中提取JWT token并验证，设置Spring Security认证上下文
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 获取Authorization请求头
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 提取token
            String token = authHeader.substring(7);
            
            // 验证token
            if (jwtUtil.validateToken(token)) {
                // 从token中获取用户信息
                Long userId = jwtUtil.getUserIdFromToken(token);
                Integer userType = jwtUtil.getUserTypeFromToken(token);
                
                if (userId != null) {
                    // 根据用户类型设置角色
                    String role = "ROLE_USER";
                    if (userType != null) {
                        switch (userType) {
                            case 1: // 个人用户
                                role = "ROLE_PERSONAL";
                                break;
                            case 2: // 企业用户
                                role = "ROLE_ENTERPRISE";
                                break;
                            case 3: // 管理员
                                role = "ROLE_ADMIN";
                                break;
                        }
                    }
                    
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userId.toString(), 
                            null, 
                            Collections.singletonList(new SimpleGrantedAuthority(role))
                        );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置到Spring Security上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
}

