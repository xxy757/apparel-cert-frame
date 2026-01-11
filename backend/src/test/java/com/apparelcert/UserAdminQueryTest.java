package com.apparelcert;

import com.apparelcert.entity.UserAdmin;
import com.apparelcert.mapper.UserAdminMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 测试查询管理员账号密码
 */
@SpringBootTest
public class UserAdminQueryTest {

    @Autowired
    private UserAdminMapper userAdminMapper;

    @Test
    public void testQueryAdminPassword() {
        // 手动创建 BCryptPasswordEncoder
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // 查询 admin 用户
        UserAdmin admin = userAdminMapper.findByUsername("admin");

        if (admin != null) {
            System.out.println("========== Admin 账号信息 ==========");
            System.out.println("ID: " + admin.getId());
            System.out.println("用户名: " + admin.getUsername());
            System.out.println("密码（BCrypt加密）: " + admin.getPassword());
            System.out.println("真实姓名: " + admin.getName());
            System.out.println("手机号: " + admin.getPhone());
            System.out.println("邮箱: " + admin.getEmail());
            System.out.println("管理员类型: " + admin.getAdminType());
            System.out.println("=================================");

            // 验证密码是否匹配 admin123
            boolean matches = passwordEncoder.matches("admin123", admin.getPassword());
            System.out.println("密码验证（admin123）: " + (matches ? "✓ 匹配" : "✗ 不匹配"));
        } else {
            System.out.println("未找到 admin 用户");
        }
    }

    @Test
    public void testCrackAdminPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 查询 admin 用户
        UserAdmin admin = userAdminMapper.findByUsername("admin");
        String targetHash = admin.getPassword();

        System.out.println("========== 开始暴力破解密码 ==========");
        System.out.println("目标哈希: " + targetHash);

        // 常见弱密码列表
        String[] commonPasswords = {
            "admin", "admin123", "password", "123456",
            "12345678", "123456789", "qwerty", "abc123",
            "111111", "123123", "admin1234", "admin888",
            "root", "toor", "pass123", "password123",
            "admin@123", "Admin123", "ADMIN123", "admin123456"
        };

        boolean found = false;
        for (String pwd : commonPasswords) {
            if (encoder.matches(pwd, targetHash)) {
                System.out.println("✓✓✓ 找到密码: [" + pwd + "]");
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("✗ 未找到匹配的密码（常见密码列表中无匹配）");
            System.out.println("\n生成正确的 admin123 BCrypt 哈希用于对比:");
            String correctHash = encoder.encode("admin123");
            System.out.println("admin123 -> " + correctHash);
        }
        System.out.println("==================================");
    }

    @Test
    public void testVerifyPassword() {
        // 生成新的 BCrypt 密码
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("========== BCrypt 密码生成 ==========");
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后密码: " + encodedPassword);
        System.out.println("验证结果: " + encoder.matches(rawPassword, encodedPassword));
        System.out.println("==================================");
    }
}
