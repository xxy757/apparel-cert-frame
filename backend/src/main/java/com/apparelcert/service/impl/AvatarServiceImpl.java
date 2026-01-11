package com.apparelcert.service.impl;

import com.apparelcert.service.AvatarService;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.UUID;

/**
 * 头像服务实现类
 * 使用 DiceBear 免费API生成卡通头像
 * 官网: https://www.dicebear.com/
 */
@Service
public class AvatarServiceImpl implements AvatarService {

    // DiceBear API 基础URL - 免费，无需API Key
    private static final String DICEBEAR_BASE_URL = "https://api.dicebear.com/9.x";

    // 头像风格（可爱卡通风格）
    private static final String AVATAR_STYLE = "adventurer";

    @Override
    public String generateRandomAvatar() {
        // 生成随机种子
        String seed = UUID.randomUUID().toString().substring(0, 8);
        return buildAvatarUrl(seed);
    }

    @Override
    public String generateAvatarByUsername(String username) {
        // 基于用户名生成确定性种子（同一用户名总是相同头像）
        String seed = generateSeedFromString(username);
        return buildAvatarUrl(seed);
    }

    @Override
    public String generateLogoByCompanyName(String companyName) {
        // 基于企业名称生成确定性种子，使用卡通头像风格
        String seed = generateSeedFromString(companyName);
        return buildAvatarUrl(seed);
    }

    /**
     * 构建头像URL
     */
    private String buildAvatarUrl(String seed) {
        return String.format("%s/%s/svg?seed=%s&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf",
                DICEBEAR_BASE_URL, AVATAR_STYLE, seed);
    }

    /**
     * 从字符串生成确定性种子
     */
    private String generateSeedFromString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            // 取前8个字符作为种子
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            // 降级处理：使用字符串hashCode
            return String.valueOf(input.hashCode());
        }
    }
}
