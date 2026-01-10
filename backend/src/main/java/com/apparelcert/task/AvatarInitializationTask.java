package com.apparelcert.task;

import com.apparelcert.service.BatchUpdateAvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 头像初始化任务 - 应用启动时执行
 */
@Component
public class AvatarInitializationTask implements CommandLineRunner {

    @Autowired
    private BatchUpdateAvatarService batchUpdateAvatarService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("开始执行头像初始化任务...");
        
        // 为所有个人用户生成头像
        batchUpdateAvatarService.updatePersonalUserAvatars();
        
        // 为企业用户生成Logo
        batchUpdateAvatarService.updateEnterpriseUserLogos();
        
        System.out.println("头像初始化任务完成！");
    }
}