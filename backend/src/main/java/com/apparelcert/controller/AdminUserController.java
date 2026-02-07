package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.UserAdmin;
import com.apparelcert.entity.UserEnterprise;
import com.apparelcert.entity.UserPersonal;
import com.apparelcert.mapper.CertificationMapper;
import com.apparelcert.mapper.UserAdminMapper;
import com.apparelcert.mapper.UserEnterpriseMapper;
import com.apparelcert.mapper.UserPersonalMapper;
import com.apparelcert.service.UserAdminService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Date;
import java.util.Map;
import java.util.List;

/**
 * 管理员用户管理控制器
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {
    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    @Autowired
    private UserPersonalMapper userPersonalMapper;

    @Autowired
    private UserEnterpriseMapper userEnterpriseMapper;

    @Autowired
    private UserAdminMapper userAdminMapper;

    @Autowired
    private UserAdminService userAdminService;

    @Autowired
    private CertificationMapper certificationMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 获取个人用户列表
     */
    @GetMapping("/personal")
    public Result<Page<UserPersonal>> getPersonalUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<UserPersonal> pageInfo = new Page<>(page, size);
        QueryWrapper<UserPersonal> wrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("username", keyword)
                .or().like("name", keyword)
                .or().like("email", keyword)
                .or().like("phone", keyword));
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("create_time");
        Page<UserPersonal> result = userPersonalMapper.selectPage(pageInfo, wrapper);
        
        // 隐藏密码
        result.getRecords().forEach(u -> u.setPassword(null));
        
        return Result.success(result);
    }

    /**
     * 获取企业用户列表
     */
    @GetMapping("/enterprise")
    public Result<Page<UserEnterprise>> getEnterpriseUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer authStatus,
            @RequestParam(required = false) Integer status) {
        Page<UserEnterprise> pageInfo = new Page<>(page, size);
        QueryWrapper<UserEnterprise> wrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("username", keyword)
                .or().like("company_name", keyword)
                .or().like("email", keyword)
                .or().like("contact_person", keyword));
        }
        if (authStatus != null) {
            wrapper.eq("auth_status", authStatus);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("create_time");
        Page<UserEnterprise> result = userEnterpriseMapper.selectPage(pageInfo, wrapper);
        
        // 隐藏密码
        result.getRecords().forEach(u -> u.setPassword(null));
        
        return Result.success(result);
    }

    /**
     * 获取个人用户详情
     */
    @GetMapping("/personal/detail")
    public Result<UserPersonal> getPersonalUserDetail(@RequestParam Long userId) {
        UserPersonal user = userPersonalMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    /**
     * 获取企业用户详情
     */
    @GetMapping("/enterprise/detail")
    public Result<UserEnterprise> getEnterpriseUserDetail(@RequestParam Long userId) {
        UserEnterprise user = userEnterpriseMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    /**
     * 创建个人用户
     */
    @PostMapping("/personal")
    public Result<Boolean> createPersonalUser(@RequestBody UserPersonal userPersonal) {
        if (userPersonal.getUsername() == null || userPersonal.getUsername().trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }
        if (userPersonal.getPassword() == null || userPersonal.getPassword().trim().isEmpty()) {
            return Result.error(400, "密码不能为空");
        }
        if (userPersonal.getName() == null || userPersonal.getName().trim().isEmpty()) {
            return Result.error(400, "姓名不能为空");
        }

        QueryWrapper<UserPersonal> usernameWrapper = new QueryWrapper<>();
        usernameWrapper.eq("username", userPersonal.getUsername());
        if (userPersonalMapper.selectOne(usernameWrapper) != null) {
            return Result.error(400, "用户名已存在");
        }

        if (userPersonal.getEmail() != null && !userPersonal.getEmail().trim().isEmpty()) {
            QueryWrapper<UserPersonal> emailWrapper = new QueryWrapper<>();
            emailWrapper.eq("email", userPersonal.getEmail());
            if (userPersonalMapper.selectOne(emailWrapper) != null) {
                return Result.error(400, "邮箱已被注册");
            }
        }

        if (userPersonal.getPhone() != null && !userPersonal.getPhone().trim().isEmpty()) {
            QueryWrapper<UserPersonal> phoneWrapper = new QueryWrapper<>();
            phoneWrapper.eq("phone", userPersonal.getPhone());
            if (userPersonalMapper.selectOne(phoneWrapper) != null) {
                return Result.error(400, "手机号已被注册");
            }
        }

        String encodedPassword = passwordEncoder.encode(userPersonal.getPassword());
        userPersonal.setPassword(encodedPassword);

        if (userPersonal.getStatus() == null) {
            userPersonal.setStatus(1);
        }

        int result = userPersonalMapper.insert(userPersonal);
        return result > 0 ? Result.success(true) : Result.error(500, "创建个人用户失败");
    }

    /**
     * 更新个人用户信息
     */
    @PutMapping("/personal")
    public Result<Boolean> updatePersonalUser(@RequestBody UserPersonal userPersonal) {
        if (userPersonal.getId() == null) {
            return Result.error(400, "用户ID不能为空");
        }
        userPersonal.setPassword(null);
        int result = userPersonalMapper.updateById(userPersonal);
        return Result.success(result > 0);
    }

    /**
     * 删除个人用户
     */
    @DeleteMapping("/personal")
    public Result<Boolean> deletePersonalUser(@RequestParam Long userId) {
        int result = userPersonalMapper.deleteById(userId);
        return Result.success(result > 0);
    }

    /**
     * 冻结个人用户
     */
    @PutMapping("/personal/freeze")
    public Result<Boolean> freezePersonalUser(@RequestParam Long userId) {
        UserPersonal user = new UserPersonal();
        user.setId(userId);
        user.setStatus(0); // 0: 冻结
        int result = userPersonalMapper.updateById(user);
        return Result.success(result > 0);
    }

    /**
     * 解冻个人用户
     */
    @PutMapping("/personal/unfreeze")
    public Result<Boolean> unfreezePersonalUser(@RequestParam Long userId) {
        UserPersonal user = new UserPersonal();
        user.setId(userId);
        user.setStatus(1); // 1: 正常
        int result = userPersonalMapper.updateById(user);
        return Result.success(result > 0);
    }

    /**
     * 重置个人用户密码
     */
    @PutMapping("/personal/reset-password")
    public Result<Boolean> resetPersonalPassword(
            @RequestParam Long userId,
            @RequestParam String newPassword) {
        UserPersonal user = userPersonalMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        int result = userPersonalMapper.updateById(user);
        return Result.success(result > 0);
    }

    /**
     * 审核企业用户
     */
    @PutMapping("/enterprise/audit")
    public Result<Boolean> auditEnterpriseUser(
            @RequestParam Long userId,
            @RequestParam Integer authStatus,
            @RequestParam(required = false) String auditRemark) {
        UserEnterprise user = new UserEnterprise();
        user.setId(userId);
        user.setAuthStatus(authStatus); // 1: 通过, 2: 拒绝
        // 可以添加审核备注字段
        int result = userEnterpriseMapper.updateById(user);
        return Result.success(result > 0);
    }

    /**
     * 创建企业用户
     */
    @PostMapping("/enterprise")
    public Result<Boolean> createEnterpriseUser(@RequestBody UserEnterprise userEnterprise) {
        if (userEnterprise.getUsername() == null || userEnterprise.getUsername().trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }
        if (userEnterprise.getPassword() == null || userEnterprise.getPassword().trim().isEmpty()) {
            return Result.error(400, "密码不能为空");
        }
        if (userEnterprise.getCompanyName() == null || userEnterprise.getCompanyName().trim().isEmpty()) {
            return Result.error(400, "企业名称不能为空");
        }

        QueryWrapper<UserEnterprise> usernameWrapper = new QueryWrapper<>();
        usernameWrapper.eq("username", userEnterprise.getUsername());
        if (userEnterpriseMapper.selectOne(usernameWrapper) != null) {
            return Result.error(400, "用户名已存在");
        }

        if (userEnterprise.getEmail() != null && !userEnterprise.getEmail().trim().isEmpty()) {
            QueryWrapper<UserEnterprise> emailWrapper = new QueryWrapper<>();
            emailWrapper.eq("email", userEnterprise.getEmail());
            if (userEnterpriseMapper.selectOne(emailWrapper) != null) {
                return Result.error(400, "邮箱已被注册");
            }
        }

        if (userEnterprise.getContactPhone() != null && !userEnterprise.getContactPhone().trim().isEmpty()) {
            QueryWrapper<UserEnterprise> phoneWrapper = new QueryWrapper<>();
            phoneWrapper.eq("contact_phone", userEnterprise.getContactPhone());
            if (userEnterpriseMapper.selectOne(phoneWrapper) != null) {
                return Result.error(400, "手机号已被注册");
            }
        }

        String encodedPassword = passwordEncoder.encode(userEnterprise.getPassword());
        userEnterprise.setPassword(encodedPassword);

        if (userEnterprise.getAuthStatus() == null) {
            userEnterprise.setAuthStatus(1);
        }
        if (userEnterprise.getStatus() == null) {
            userEnterprise.setStatus(1);
        }

        int result = userEnterpriseMapper.insert(userEnterprise);
        return result > 0 ? Result.success(true) : Result.error(500, "创建企业用户失败");
    }

    /**
     * 更新企业用户信息
     */
    @PutMapping("/enterprise")
    public Result<Boolean> updateEnterpriseUser(@RequestBody UserEnterprise userEnterprise) {
        if (userEnterprise.getId() == null) {
            return Result.error(400, "用户ID不能为空");
        }
        userEnterprise.setPassword(null);
        int result = userEnterpriseMapper.updateById(userEnterprise);
        return Result.success(result > 0);
    }

    /**
     * 删除企业用户
     */
    @DeleteMapping("/enterprise")
    public Result<Boolean> deleteEnterpriseUser(@RequestParam Long userId) {
        int result = userEnterpriseMapper.deleteById(userId);
        return Result.success(result > 0);
    }

    /**
     * 冻结企业用户
     */
    @PutMapping("/enterprise/freeze")
    public Result<Boolean> freezeEnterpriseUser(@RequestParam Long userId) {
        UserEnterprise user = new UserEnterprise();
        user.setId(userId);
        user.setStatus(0);
        int result = userEnterpriseMapper.updateById(user);
        return Result.success(result > 0);
    }

    /**
     * 解冻企业用户
     */
    @PutMapping("/enterprise/unfreeze")
    public Result<Boolean> unfreezeEnterpriseUser(@RequestParam Long userId) {
        UserEnterprise user = new UserEnterprise();
        user.setId(userId);
        user.setStatus(1);
        int result = userEnterpriseMapper.updateById(user);
        return Result.success(result > 0);
    }

    /**
     * 重置企业用户密码
     */
    @PutMapping("/enterprise/reset-password")
    public Result<Boolean> resetEnterprisePassword(
            @RequestParam Long userId,
            @RequestParam String newPassword) {
        UserEnterprise user = userEnterpriseMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        int result = userEnterpriseMapper.updateById(user);
        return Result.success(result > 0);
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 个人用户统计
        QueryWrapper<UserPersonal> personalWrapper = new QueryWrapper<>();
        long totalPersonal = userPersonalMapper.selectCount(personalWrapper);
        stats.put("totalPersonalUsers", totalPersonal);
        
        personalWrapper.eq("status", 1);
        long activePersonal = userPersonalMapper.selectCount(personalWrapper);
        stats.put("activePersonalUsers", activePersonal);
        stats.put("disabledPersonalUsers", totalPersonal - activePersonal);
        
        // 企业用户统计
        QueryWrapper<UserEnterprise> enterpriseWrapper = new QueryWrapper<>();
        long totalEnterprise = userEnterpriseMapper.selectCount(enterpriseWrapper);
        stats.put("totalEnterpriseUsers", totalEnterprise);

        QueryWrapper<UserEnterprise> activeEnterpriseWrapper = new QueryWrapper<>();
        activeEnterpriseWrapper.eq("status", 1);
        long activeEnterprise = userEnterpriseMapper.selectCount(activeEnterpriseWrapper);
        stats.put("activeEnterpriseUsers", activeEnterprise);
        stats.put("disabledEnterpriseUsers", totalEnterprise - activeEnterprise);
        
        QueryWrapper<UserEnterprise> pendingWrapper = new QueryWrapper<>();
        pendingWrapper.eq("auth_status", 0);
        long pendingEnterprise = userEnterpriseMapper.selectCount(pendingWrapper);
        stats.put("pendingEnterpriseUsers", pendingEnterprise);
        
        QueryWrapper<UserEnterprise> approvedWrapper = new QueryWrapper<>();
        approvedWrapper.eq("auth_status", 1);
        long approvedEnterprise = userEnterpriseMapper.selectCount(approvedWrapper);
        stats.put("approvedEnterpriseUsers", approvedEnterprise);

        // 管理员用户统计
        QueryWrapper<UserAdmin> adminWrapper = new QueryWrapper<>();
        long totalAdmin = userAdminMapper.selectCount(adminWrapper);
        stats.put("totalAdminUsers", totalAdmin);

        QueryWrapper<UserAdmin> activeAdminWrapper = new QueryWrapper<>();
        activeAdminWrapper.eq("status", 1);
        long activeAdmin = userAdminMapper.selectCount(activeAdminWrapper);
        stats.put("activeAdminUsers", activeAdmin);
        stats.put("disabledAdminUsers", totalAdmin - activeAdmin);
        
        return Result.success(stats);
    }

    /**
     * 获取用户注册趋势（按天统计）
     */
    @GetMapping("/statistics/trend")
    public Result<Map<String, Object>> getUserRegistrationTrend(
            @RequestParam(defaultValue = "7") Integer days) {
        Map<String, Object> result = new HashMap<>();
        
        java.util.List<Map<String, Object>> personalTrend = new java.util.ArrayList<>();
        java.util.List<Map<String, Object>> enterpriseTrend = new java.util.ArrayList<>();
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd");
        
        for (int i = days - 1; i >= 0; i--) {
            java.util.Calendar dayCal = (java.util.Calendar) cal.clone();
            dayCal.add(java.util.Calendar.DAY_OF_MONTH, -i);
            java.util.Date dayStart = dayCal.getTime();
            dayCal.add(java.util.Calendar.DAY_OF_MONTH, 1);
            java.util.Date dayEnd = dayCal.getTime();
            
            String dateLabel = sdf.format(dayStart);
            
            // 个人用户注册数
            QueryWrapper<UserPersonal> pWrapper = new QueryWrapper<>();
            pWrapper.ge("create_time", dayStart);
            pWrapper.lt("create_time", dayEnd);
            long pCount = userPersonalMapper.selectCount(pWrapper);
            
            Map<String, Object> pData = new HashMap<>();
            pData.put("date", dateLabel);
            pData.put("count", pCount);
            personalTrend.add(pData);
            
            // 企业用户注册数
            QueryWrapper<UserEnterprise> eWrapper = new QueryWrapper<>();
            eWrapper.ge("create_time", dayStart);
            eWrapper.lt("create_time", dayEnd);
            long eCount = userEnterpriseMapper.selectCount(eWrapper);
            
            Map<String, Object> eData = new HashMap<>();
            eData.put("date", dateLabel);
            eData.put("count", eCount);
            enterpriseTrend.add(eData);
        }
        
        result.put("personalTrend", personalTrend);
        result.put("enterpriseTrend", enterpriseTrend);

        // 当月新增（用于前端汇总展示）
        java.util.Calendar monthCal = java.util.Calendar.getInstance();
        monthCal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        monthCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        monthCal.set(java.util.Calendar.MINUTE, 0);
        monthCal.set(java.util.Calendar.SECOND, 0);
        java.util.Date monthStart = monthCal.getTime();

        QueryWrapper<UserPersonal> monthPersonalWrapper = new QueryWrapper<>();
        monthPersonalWrapper.ge("create_time", monthStart);
        long monthPersonal = userPersonalMapper.selectCount(monthPersonalWrapper);

        QueryWrapper<UserEnterprise> monthEnterpriseWrapper = new QueryWrapper<>();
        monthEnterpriseWrapper.ge("create_time", monthStart);
        long monthEnterprise = userEnterpriseMapper.selectCount(monthEnterpriseWrapper);

        result.put("monthlyNew", monthPersonal + monthEnterprise);
        
        return Result.success(result);
    }
    
    /**
     * 获取用户活跃度统计
     */
    @GetMapping("/statistics/activity")
    public Result<Map<String, Object>> getUserActivityStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        
        // 今日新增
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        java.util.Date today = cal.getTime();
        
        QueryWrapper<UserPersonal> todayPersonalWrapper = new QueryWrapper<>();
        todayPersonalWrapper.ge("create_time", today);
        long todayPersonal = userPersonalMapper.selectCount(todayPersonalWrapper);
        stats.put("todayNewPersonal", todayPersonal);
        
        QueryWrapper<UserEnterprise> todayEnterpriseWrapper = new QueryWrapper<>();
        todayEnterpriseWrapper.ge("create_time", today);
        long todayEnterprise = userEnterpriseMapper.selectCount(todayEnterpriseWrapper);
        stats.put("todayNewEnterprise", todayEnterprise);
        
        // 本周新增
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        java.util.Date weekStart = cal.getTime();
        
        QueryWrapper<UserPersonal> weekPersonalWrapper = new QueryWrapper<>();
        weekPersonalWrapper.ge("create_time", weekStart);
        long weekPersonal = userPersonalMapper.selectCount(weekPersonalWrapper);
        stats.put("weekNewPersonal", weekPersonal);
        
        QueryWrapper<UserEnterprise> weekEnterpriseWrapper = new QueryWrapper<>();
        weekEnterpriseWrapper.ge("create_time", weekStart);
        long weekEnterprise = userEnterpriseMapper.selectCount(weekEnterpriseWrapper);
        stats.put("weekNewEnterprise", weekEnterprise);
        
        // 本月新增
        cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        java.util.Date monthStart = cal.getTime();
        
        QueryWrapper<UserPersonal> monthPersonalWrapper = new QueryWrapper<>();
        monthPersonalWrapper.ge("create_time", monthStart);
        long monthPersonal = userPersonalMapper.selectCount(monthPersonalWrapper);
        stats.put("monthNewPersonal", monthPersonal);
        
        QueryWrapper<UserEnterprise> monthEnterpriseWrapper = new QueryWrapper<>();
        monthEnterpriseWrapper.ge("create_time", monthStart);
        long monthEnterprise = userEnterpriseMapper.selectCount(monthEnterpriseWrapper);
        stats.put("monthNewEnterprise", monthEnterprise);

        long totalPersonal = userPersonalMapper.selectCount(new QueryWrapper<>());
        long totalEnterprise = userEnterpriseMapper.selectCount(new QueryWrapper<>());
        long totalUsers = totalPersonal + totalEnterprise;

        QueryWrapper<UserPersonal> activePersonalWrapper = new QueryWrapper<>();
        activePersonalWrapper.eq("status", 1);
        long activePersonal = userPersonalMapper.selectCount(activePersonalWrapper);

        QueryWrapper<UserEnterprise> activeEnterpriseWrapper = new QueryWrapper<>();
        activeEnterpriseWrapper.eq("status", 1);
        long activeEnterprise = userEnterpriseMapper.selectCount(activeEnterpriseWrapper);

        long dailyActive = activePersonal + activeEnterprise;
        double retentionRate = totalUsers == 0 ? 0 : (dailyActive * 100.0 / totalUsers);

        double certificationRate = 0;
        try {
            QueryWrapper<com.apparelcert.entity.Certification> certWrapper = new QueryWrapper<>();
            certWrapper.select("distinct user_id");
            // certification.status: 2-已通过, 3-未通过
            certWrapper.eq("status", 2);
            List<Object> certifiedUsers = certificationMapper.selectObjs(certWrapper);
            long certifiedCount = certifiedUsers != null ? certifiedUsers.size() : 0;
            certificationRate = totalPersonal == 0 ? 0 : (certifiedCount * 100.0 / totalPersonal);
        } catch (Exception ex) {
            log.warn("统计企业认证率失败，降级为0: {}", ex.getMessage());
        }

        stats.put("dailyActive", dailyActive);
        stats.put("retentionRate", Math.round(retentionRate * 10.0) / 10.0);
        stats.put("certificationRate", Math.round(certificationRate * 10.0) / 10.0);
        
        return Result.success(stats);
    }

    /**
     * 批量冻结个人用户
     */
    @PutMapping("/personal/batch-freeze")
    public Result<Integer> batchFreezePersonalUsers(@RequestBody java.util.List<Long> userIds) {
        int count = 0;
        for (Long userId : userIds) {
            UserPersonal user = new UserPersonal();
            user.setId(userId);
            user.setStatus(0);
            if (userPersonalMapper.updateById(user) > 0) {
                count++;
            }
        }
        return Result.success(count);
    }

    /**
     * 批量解冻个人用户
     */
    @PutMapping("/personal/batch-unfreeze")
    public Result<Integer> batchUnfreezePersonalUsers(@RequestBody java.util.List<Long> userIds) {
        int count = 0;
        for (Long userId : userIds) {
            UserPersonal user = new UserPersonal();
            user.setId(userId);
            user.setStatus(1);
            if (userPersonalMapper.updateById(user) > 0) {
                count++;
            }
        }
        return Result.success(count);
    }

    /**
     * 批量冻结企业用户
     */
    @PutMapping("/enterprise/batch-freeze")
    public Result<Integer> batchFreezeEnterpriseUsers(@RequestBody java.util.List<Long> userIds) {
        int count = 0;
        for (Long userId : userIds) {
            UserEnterprise user = new UserEnterprise();
            user.setId(userId);
            user.setStatus(0);
            if (userEnterpriseMapper.updateById(user) > 0) {
                count++;
            }
        }
        return Result.success(count);
    }

    /**
     * 批量解冻企业用户
     */
    @PutMapping("/enterprise/batch-unfreeze")
    public Result<Integer> batchUnfreezeEnterpriseUsers(@RequestBody java.util.List<Long> userIds) {
        int count = 0;
        for (Long userId : userIds) {
            UserEnterprise user = new UserEnterprise();
            user.setId(userId);
            user.setStatus(1);
            if (userEnterpriseMapper.updateById(user) > 0) {
                count++;
            }
        }
        return Result.success(count);
    }

    /**
     * 批量冻结管理员用户
     */
    @PutMapping("/admin/batch-freeze")
    public Result<Integer> batchFreezeAdminUsers(@RequestBody java.util.List<Long> adminIds) {
        int count = 0;
        for (Long adminId : adminIds) {
            UserAdmin user = new UserAdmin();
            user.setId(adminId);
            user.setStatus(0);
            if (userAdminMapper.updateById(user) > 0) {
                count++;
            }
        }
        return Result.success(count);
    }

    /**
     * 批量解冻管理员用户
     */
    @PutMapping("/admin/batch-unfreeze")
    public Result<Integer> batchUnfreezeAdminUsers(@RequestBody java.util.List<Long> adminIds) {
        int count = 0;
        for (Long adminId : adminIds) {
            UserAdmin user = new UserAdmin();
            user.setId(adminId);
            user.setStatus(1);
            if (userAdminMapper.updateById(user) > 0) {
                count++;
            }
        }
        return Result.success(count);
    }

    // ==================== 管理员用户管理接口 ====================

    /**
     * 创建管理员用户（仅超级管理员可用）
     */
    @PostMapping("/admin")
    public Result<Boolean> createAdmin(@RequestBody UserAdmin userAdmin) {
        // 检查用户名是否已存在
        UserAdmin existingAdmin = userAdminMapper.findByUsername(userAdmin.getUsername());
        if (existingAdmin != null) {
            return Result.error(400, "用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userAdmin.getEmail() != null && !userAdmin.getEmail().isEmpty()) {
            UserAdmin existingEmail = userAdminMapper.findByEmail(userAdmin.getEmail());
            if (existingEmail != null) {
                return Result.error(400, "邮箱已被注册");
            }
        }

        // 加密密码
        String encodedPassword = passwordEncoder.encode(userAdmin.getPassword());
        userAdmin.setPassword(encodedPassword);

        // 设置默认值
        if (userAdmin.getAdminType() == null) {
            userAdmin.setAdminType(1); // 默认为普通管理员
        }
        userAdmin.setStatus(1); // 默认启用

        int result = userAdminMapper.insert(userAdmin);
        if (result > 0) {
            return Result.success(true);
        } else {
            return Result.error(500, "创建管理员失败");
        }
    }

    /**
     * 获取管理员用户列表
     */
    @GetMapping("/admin")
    public Result<Page<UserAdmin>> getAdminUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer adminType,
            @RequestParam(required = false) Integer status) {
        Page<UserAdmin> pageInfo = new Page<>(page, size);
        QueryWrapper<UserAdmin> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("username", keyword)
                .or().like("name", keyword)
                .or().like("email", keyword)
                .or().like("phone", keyword));
        }
        if (adminType != null) {
            wrapper.eq("admin_type", adminType);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("create_time");
        Page<UserAdmin> result = userAdminMapper.selectPage(pageInfo, wrapper);

        // 隐藏密码
        result.getRecords().forEach(u -> u.setPassword(null));

        return Result.success(result);
    }

    /**
     * 获取管理员用户详情
     */
    @GetMapping("/admin/detail")
    public Result<UserAdmin> getAdminUserDetail(@RequestParam Long userId) {
        UserAdmin user = userAdminMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    /**
     * 更新管理员用户信息
     */
    @PutMapping("/admin")
    public Result<Boolean> updateAdmin(@RequestBody UserAdmin userAdmin) {
        // 不允许通过此接口修改密码
        userAdmin.setPassword(null);

        int result = userAdminMapper.updateById(userAdmin);
        return Result.success(result > 0);
    }

    /**
     * 删除管理员用户
     */
    @DeleteMapping("/admin")
    public Result<Boolean> deleteAdminUser(@RequestParam Long adminId) {
        int result = userAdminMapper.deleteById(adminId);
        return Result.success(result > 0);
    }

    /**
     * 管理员修改自己的密码
     */
    @PutMapping("/admin/change-password")
    public Result<Boolean> changeAdminPassword(@RequestBody Map<String, String> params) {
        Long adminId = Long.parseLong(params.get("adminId"));
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return Result.error(400, "参数不完整");
        }

        boolean result = userAdminService.changePassword(adminId, oldPassword, newPassword);
        if (result) {
            return Result.success(true);
        } else {
            return Result.error(400, "旧密码错误或修改失败");
        }
    }

    /**
     * 重置管理员密码（超级管理员可用）
     */
    @PutMapping("/admin/reset-password")
    public Result<Boolean> resetAdminPassword(
            @RequestParam Long adminId,
            @RequestParam String newPassword) {
        UserAdmin admin = userAdminMapper.selectById(adminId);
        if (admin == null) {
            return Result.error(404, "管理员不存在");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        admin.setPassword(encodedPassword);

        int result = userAdminMapper.updateById(admin);
        return Result.success(result > 0);
    }

    /**
     * 冻结管理员用户
     */
    @PutMapping("/admin/freeze")
    public Result<Boolean> freezeAdminUser(@RequestParam Long adminId) {
        UserAdmin user = new UserAdmin();
        user.setId(adminId);
        user.setStatus(0); // 0: 冻结
        int result = userAdminMapper.updateById(user);
        return Result.success(result > 0);
    }

    /**
     * 解冻管理员用户
     */
    @PutMapping("/admin/unfreeze")
    public Result<Boolean> unfreezeAdminUser(@RequestParam Long adminId) {
        UserAdmin user = new UserAdmin();
        user.setId(adminId);
        user.setStatus(1); // 1: 正常
        int result = userAdminMapper.updateById(user);
        return Result.success(result > 0);
    }
}
