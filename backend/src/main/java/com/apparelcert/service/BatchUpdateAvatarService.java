package com.apparelcert.service;

import com.apparelcert.entity.UserEnterprise;
import com.apparelcert.entity.UserPersonal;
import com.apparelcert.mapper.UserEnterpriseMapper;
import com.apparelcert.mapper.UserPersonalMapper;
import com.apparelcert.service.AvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 批量更新用户头像服务
 */
@Service
public class BatchUpdateAvatarService {

    @Autowired
    private UserPersonalMapper userPersonalMapper;

    @Autowired
    private UserEnterpriseMapper userEnterpriseMapper;

    @Autowired
    private AvatarService avatarService;

    /**
     * 为所有个人用户生成头像
     */
    public void updatePersonalUserAvatars() {
        // 获取所有个人用户
        List<UserPersonal> users = userPersonalMapper.selectList(null);
        
        System.out.println("开始为个人用户生成头像，共找到 " + users.size() + " 个用户");
        
        int updatedCount = 0;
        for (UserPersonal user : users) {
            // 检查是否已有头像
            if (user.getAvatar() == null || user.getAvatar().trim().isEmpty()) {
                // 生成基于用户名的头像URL
                String avatarUrl = avatarService.generateAvatarByUsername(user.getUsername());
                
                // 更新头像
                user.setAvatar(avatarUrl);
                userPersonalMapper.updateById(user);
                
                updatedCount++;
                System.out.println("为个人用户 " + user.getUsername() + " 生成头像: " + avatarUrl);
            }
        }
        
        System.out.println("个人用户头像更新完成，共更新 " + updatedCount + " 个用户");
    }

    /**
     * 为企业用户生成Logo
     */
    public void updateEnterpriseUserLogos() {
        // 获取所有企业用户
        List<UserEnterprise> users = userEnterpriseMapper.selectList(null);
        
        System.out.println("开始为企业用户生成Logo，共找到 " + users.size() + " 个用户");
        
        int updatedCount = 0;
        for (UserEnterprise user : users) {
            // 检查是否已有Logo
            if (user.getLogo() == null || user.getLogo().trim().isEmpty()) {
                // 生成基于企业名称的Logo URL
                String logoUrl = avatarService.generateLogoByCompanyName(user.getCompanyName());
                
                // 更新Logo
                user.setLogo(logoUrl);
                userEnterpriseMapper.updateById(user);
                
                updatedCount++;
                System.out.println("为企业用户 " + user.getUsername() + " 生成Logo: " + logoUrl);
            }
        }
        
        System.out.println("企业用户Logo更新完成，共更新 " + updatedCount + " 个用户");
    }
}