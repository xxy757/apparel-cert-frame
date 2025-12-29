package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.UserEnterprise;
import com.apparelcert.entity.UserPersonal;
import com.apparelcert.mapper.UserEnterpriseMapper;
import com.apparelcert.mapper.UserPersonalMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员用户管理控制器
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private UserPersonalMapper userPersonalMapper;

    @Autowired
    private UserEnterpriseMapper userEnterpriseMapper;

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
            @RequestParam(required = false) Integer authStatus) {
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
        
        // 企业用户统计
        QueryWrapper<UserEnterprise> enterpriseWrapper = new QueryWrapper<>();
        long totalEnterprise = userEnterpriseMapper.selectCount(enterpriseWrapper);
        stats.put("totalEnterpriseUsers", totalEnterprise);
        
        QueryWrapper<UserEnterprise> pendingWrapper = new QueryWrapper<>();
        pendingWrapper.eq("auth_status", 0);
        long pendingEnterprise = userEnterpriseMapper.selectCount(pendingWrapper);
        stats.put("pendingEnterpriseUsers", pendingEnterprise);
        
        QueryWrapper<UserEnterprise> approvedWrapper = new QueryWrapper<>();
        approvedWrapper.eq("auth_status", 1);
        long approvedEnterprise = userEnterpriseMapper.selectCount(approvedWrapper);
        stats.put("approvedEnterpriseUsers", approvedEnterprise);
        
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
}
