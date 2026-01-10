package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.UserPersonal;
import com.apparelcert.service.UserPersonalService;
import com.apparelcert.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 个人用户控制器
 */
@RestController
@RequestMapping("/api/user-personal")
public class UserPersonalController {

    @Autowired
    private UserPersonalService userPersonalService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 更新个人用户基本信息
     */
    @PostMapping("/update-basic-info")
    public Result<Boolean> updateBasicInfo(@RequestHeader("Authorization") String authHeader,
                                          @RequestBody UserPersonal userPersonal) {
        try {
            // 从token中获取当前用户ID
            String token = authHeader.replace("Bearer ", "");
            Long currentUserId = jwtUtil.getUserIdFromToken(token);
            Integer userType = jwtUtil.getUserTypeFromToken(token);

            if (currentUserId == null || !userPersonal.getId().equals(currentUserId) || userType != 1) {
                return Result.error(403, "无权修改他人信息");
            }

            // 验证必要参数
            if (userPersonal.getName() == null || userPersonal.getName().trim().isEmpty()) {
                return Result.error(400, "姓名不能为空");
            }

            // 更新基本信息
            boolean result = userPersonalService.updateBasicInfo(userPersonal);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "更新失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current-user")
    public Result<UserPersonal> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            Integer userType = jwtUtil.getUserTypeFromToken(token);

            if (userId == null || userType != 1) {
                return Result.error(401, "认证信息无效");
            }

            UserPersonal user = userPersonalService.getUserById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }

            return Result.success(user);
        } catch (Exception e) {
            return Result.error(500, "获取用户信息失败：" + e.getMessage());
        }
    }
}