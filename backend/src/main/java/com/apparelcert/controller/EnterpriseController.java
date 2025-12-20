package com.apparelcert.controller;

import com.apparelcert.common.Result;
import com.apparelcert.entity.UserEnterprise;
import com.apparelcert.service.UserEnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 企业用户控制器
 */
@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseController {

    @Autowired
    private UserEnterpriseService userEnterpriseService;

    /**
     * 获取企业信息
     */
    @GetMapping
    public Result<UserEnterprise> getEnterpriseInfo(@RequestParam Long enterpriseId) {
        UserEnterprise enterprise = userEnterpriseService.getById(enterpriseId);
        return Result.success(enterprise);
    }

    /**
     * 更新企业信息
     */
    @PostMapping
    public Result<Boolean> updateEnterpriseInfo(@RequestBody UserEnterprise enterprise) {
        boolean result = userEnterpriseService.updateEnterpriseInfo(enterprise);
        return Result.success(result);
    }

    /**
     * 提交企业认证
     */
    @PostMapping("/auth")
    public Result<Boolean> submitEnterpriseAuth(
            @RequestParam Long enterpriseId,
            @RequestParam String businessLicense,
            @RequestParam(required = false) String otherCertificates) {
        boolean result = userEnterpriseService.submitEnterpriseAuth(enterpriseId, businessLicense, otherCertificates);
        return Result.success(result);
    }

    /**
     * 获取企业认证状态
     */
    @GetMapping("/auth/status")
    public Result<Integer> getAuthStatus(@RequestParam Long enterpriseId) {
        Integer authStatus = userEnterpriseService.getAuthStatus(enterpriseId);
        return Result.success(authStatus);
    }

    /**
     * 更新企业认证状态 (仅管理员使用)
     */
    @PutMapping("/auth/status")
    public Result<Boolean> updateAuthStatus(@RequestParam Long enterpriseId, @RequestParam Integer authStatus) {
        boolean result = userEnterpriseService.updateAuthStatus(enterpriseId, authStatus);
        return Result.success(result);
    }
}