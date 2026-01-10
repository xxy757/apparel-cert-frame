package com.apparelcert.controller;

import com.apparelcert.service.BatchUpdateAvatarService;
import com.apparelcert.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 批量更新控制器
 */
@RestController
@RequestMapping("/api/batch-update")
public class BatchUpdateController {

    @Autowired
    private BatchUpdateAvatarService batchUpdateAvatarService;

    /**
     * 为所有个人用户生成头像
     */
    @PostMapping("/update-personal-avatars")
    public Result<String> updatePersonalUserAvatars() {
        try {
            batchUpdateAvatarService.updatePersonalUserAvatars();
            return Result.success("个人用户头像更新完成");
        } catch (Exception e) {
            return Result.error(500, "更新失败：" + e.getMessage());
        }
    }

    /**
     * 为企业用户生成Logo
     */
    @PostMapping("/update-enterprise-logos")
    public Result<String> updateEnterpriseUserLogos() {
        try {
            batchUpdateAvatarService.updateEnterpriseUserLogos();
            return Result.success("企业用户Logo更新完成");
        } catch (Exception e) {
            return Result.error(500, "更新失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新所有用户的头像和Logo
     */
    @PostMapping("/update-all-avatars")
    public Result<String> updateAllAvatars() {
        try {
            batchUpdateAvatarService.updatePersonalUserAvatars();
            batchUpdateAvatarService.updateEnterpriseUserLogos();
            return Result.success("所有用户头像和Logo更新完成");
        } catch (Exception e) {
            return Result.error(500, "更新失败：" + e.getMessage());
        }
    }
}