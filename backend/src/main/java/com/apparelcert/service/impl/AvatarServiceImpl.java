package com.apparelcert.service.impl;

import com.apparelcert.service.AvatarService;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.SecureRandom;
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

    // 个人用户头像风格（可爱卡通风格）
    private static final String PERSONAL_STYLE = "adventurer";

    // 企业Logo风格（简约商务风格）
    private static final String ENTERPRISE_STYLE = "initials";

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generateRandomAvatar() {
        // 生成随机种子
        String seed = UUID.randomUUID().toString().substring(0, 8);
        return buildAvatarUrl(PERSONAL_STYLE, seed);
    }

    @Override
    public String generateAvatarByUsername(String username) {
        // 基于用户名生成确定性种子（同一用户名总是相同头像）
        String seed = generateSeedFromString(username);
        return buildAvatarUrl(PERSONAL_STYLE, seed);
    }
    
    @Override
    public String generateRandomLogo() {
        // 生成随机种子
        String seed = UUID.randomUUID().toString().substring(0, 8);
        return buildAvatarUrl(ENTERPRISE_STYLE, seed);
    }

    @Override
    public String generateLogoByCompanyName(String companyName) {
        // 基于企业名称生成确定性种子
        String seed = generateSeedFromString(companyName);

        // 对于initials风格，使用企业名称首字母
        String initials = extractInitials(companyName);
        return buildLogoUrl(initials);
    }

    /**
     * 构建头像URL
     */
    private String buildAvatarUrl(String style, String seed) {
        return String.format("%s/%s/svg?seed=%s&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf",
                DICEBEAR_BASE_URL, style, seed);
    }

    /**
     * 构建Logo URL（使用首字母风格）
     */
    private String buildLogoUrl(String initials) {
        return String.format("%s/%s/svg?seed=%s&backgroundColor=c0aede,d1d4f4,ffd5dc,ffdfbf",
                DICEBEAR_BASE_URL, ENTERPRISE_STYLE, initials);
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

    /**
     * 提取企业名称首字母
     */
    private String extractInitials(String companyName) {
        if (companyName == null || companyName.isEmpty()) {
            return "U"; // 默认Unknown
        }

        // 移除常见的企业后缀
        String cleaned = companyName.replaceAll("(?i)(公司|有限公司|股份有限公司|科技|网络|信息|系统|工作室|厂|店)", "");
        cleaned = cleaned.trim();

        if (cleaned.isEmpty()) {
            cleaned = companyName;
        }

        // 提取中文名称首字或英文名称首字母
        StringBuilder initials = new StringBuilder();
        String[] words = cleaned.split("\\s+");

        for (String word : words) {
            if (!word.isEmpty()) {
                if (isChinese(word.charAt(0))) {
                    // 中文取第一个字
                    initials.append(word.charAt(0));
                } else {
                    // 英文取首字母
                    initials.append(Character.toUpperCase(word.charAt(0)));
                }
                if (initials.length() >= 2) {
                    break; // 最多取2个字符
                }
            }
        }

        return initials.length() > 0 ? initials.toString() : cleaned.substring(0, 1).toUpperCase();
    }

    /**
     * 判断是否为中文字符
     */
    private boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }
}
