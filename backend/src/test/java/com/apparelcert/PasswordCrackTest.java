package com.apparelcert;

import com.apparelcert.entity.UserAdmin;
import com.apparelcert.entity.UserEnterprise;
import com.apparelcert.entity.UserPersonal;
import com.apparelcert.mapper.UserAdminMapper;
import com.apparelcert.mapper.UserEnterpriseMapper;
import com.apparelcert.mapper.UserPersonalMapper;
import com.apparelcert.service.AvatarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;

/**
 * 密码破解/查询测试类
 * 用于查询数据库中加密密码的实际明文值
 */
@SpringBootTest
public class PasswordCrackTest {

    @Autowired
    private UserAdminMapper userAdminMapper;

    @Autowired
    private UserPersonalMapper userPersonalMapper;

    @Autowired
    private UserEnterpriseMapper userEnterpriseMapper;

    @Autowired
    private AvatarService avatarService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 常见弱密码列表
    private static final String[] COMMON_PASSWORDS = {
        "admin", "admin123", "password", "123456",
        "12345678", "123456789", "qwerty", "abc123",
        "111111", "123123", "admin1234", "admin888",
        "root", "toor", "pass123", "password123",
        "admin@123", "Admin123", "ADMIN123", "admin123456",
        "test123", "1234", "12345", "pass", "test"
    };

    /**
     * 破解管理员密码
     */
    @Test
    public void testCrackAdminPassword() {
        System.out.println("\n========== 管理员密码破解 ==========");

        List<UserAdmin> admins = userAdminMapper.selectList(null);

        for (UserAdmin admin : admins) {
            System.out.println("\n-------------------------------------");
            System.out.println("用户名: " + admin.getUsername());
            System.out.println("姓名: " + admin.getName());
            System.out.println("哈希值: " + admin.getPassword());

            String plainPassword = crackPassword(admin.getPassword());
            if (plainPassword != null) {
                System.out.println("✓ 密码: [" + plainPassword + "]");
            } else {
                System.out.println("✗ 未在常见密码列表中找到匹配");
            }
        }
        System.out.println("\n====================================");
    }

    /**
     * 破解个人用户密码
     */
    @Test
    public void testCrackPersonalUserPassword() {
        System.out.println("\n========== 个人用户密码破解 ==========");

        List<UserPersonal> users = userPersonalMapper.selectList(null);

        for (UserPersonal user : users) {
            System.out.println("\n-------------------------------------");
            System.out.println("用户名: " + user.getUsername());
            System.out.println("姓名: " + user.getName());
            System.out.println("哈希值: " + user.getPassword());

            String plainPassword = crackPassword(user.getPassword());
            if (plainPassword != null) {
                System.out.println("✓ 密码: [" + plainPassword + "]");
            } else {
                System.out.println("✗ 未在常见密码列表中找到匹配");
            }
        }
        System.out.println("\n======================================");
    }

    /**
     * 破解企业用户密码
     */
    @Test
    public void testCrackEnterpriseUserPassword() {
        System.out.println("\n========== 企业用户密码破解 ==========");

        List<UserEnterprise> users = userEnterpriseMapper.selectList(null);

        for (UserEnterprise user : users) {
            System.out.println("\n-------------------------------------");
            System.out.println("用户名: " + user.getUsername());
            System.out.println("企业名称: " + user.getCompanyName());
            System.out.println("哈希值: " + user.getPassword());

            String plainPassword = crackPassword(user.getPassword());
            if (plainPassword != null) {
                System.out.println("✓ 密码: [" + plainPassword + "]");
            } else {
                System.out.println("✗ 未在常见密码列表中找到匹配");
            }
        }
        System.out.println("\n======================================");
    }

    /**
     * 破解所有用户密码
     */
    @Test
    public void testCrackAllPasswords() {
        System.out.println("\n========== 全部用户密码破解 ==========");

        // 管理员
        System.out.println("\n--- 管理员 ---");
        List<UserAdmin> admins = userAdminMapper.selectList(null);
        for (UserAdmin admin : admins) {
            String plain = crackPassword(admin.getPassword());
            System.out.printf("  %-15s -> %s%n", admin.getUsername(),
                plain != null ? "[" + plain + "]" : "✗ 未找到");
        }

        // 个人用户
        System.out.println("\n--- 个人用户 ---");
        List<UserPersonal> personalUsers = userPersonalMapper.selectList(null);
        for (UserPersonal user : personalUsers) {
            String plain = crackPassword(user.getPassword());
            System.out.printf("  %-15s -> %s%n", user.getUsername(),
                plain != null ? "[" + plain + "]" : "✗ 未找到");
        }

        // 企业用户
        System.out.println("\n--- 企业用户 ---");
        List<UserEnterprise> enterpriseUsers = userEnterpriseMapper.selectList(null);
        for (UserEnterprise user : enterpriseUsers) {
            String plain = crackPassword(user.getPassword());
            System.out.printf("  %-15s -> %s%n", user.getUsername(),
                plain != null ? "[" + plain + "]" : "✗ 未找到");
        }

        System.out.println("\n======================================");
    }

    /**
     * 根据用户名破解密码（支持所有用户类型）
     */
    @Test
    public void testCrackByUsername() {
        String username = "admin"; // 修改这里要查询的用户名

        System.out.println("\n========== 按用户名破解密码 ==========");
        System.out.println("查询用户名: " + username);

        // 尝试管理员
        UserAdmin admin = userAdminMapper.findByUsername(username);
        if (admin != null) {
            String plain = crackPassword(admin.getPassword());
            System.out.println("类型: 管理员");
            System.out.println("姓名: " + admin.getName());
            System.out.println("哈希: " + admin.getPassword());
            System.out.println("密码: " + (plain != null ? "[" + plain + "]" : "✗ 未找到"));
            return;
        }

        // 尝试个人用户
        UserPersonal personal = userPersonalMapper.findByUsername(username);
        if (personal != null) {
            String plain = crackPassword(personal.getPassword());
            System.out.println("类型: 个人用户");
            System.out.println("姓名: " + personal.getName());
            System.out.println("哈希: " + personal.getPassword());
            System.out.println("密码: " + (plain != null ? "[" + plain + "]" : "✗ 未找到"));
            return;
        }

        // 尝试企业用户
        UserEnterprise enterprise = userEnterpriseMapper.findByUsername(username);
        if (enterprise != null) {
            String plain = crackPassword(enterprise.getPassword());
            System.out.println("类型: 企业用户");
            System.out.println("企业名: " + enterprise.getCompanyName());
            System.out.println("哈希: " + enterprise.getPassword());
            System.out.println("密码: " + (plain != null ? "[" + plain + "]" : "✗ 未找到"));
            return;
        }

        System.out.println("✗ 未找到用户: " + username);
        System.out.println("====================================");
    }

    /**
     * 生成指定密码的 BCrypt 哈希值
     */
    @Test
    public void testGenerateHash() {
        String plainPassword = "admin123"; // 修改这里要生成哈希的密码

        System.out.println("\n========== 生成 BCrypt 哈希 ==========");
        System.out.println("明文密码: " + plainPassword);

        // 生成多次（BCrypt 每次生成不同的盐，哈希值会不同）
        System.out.println("\n生成的哈希值（每次不同，但都有效）:");
        for (int i = 0; i < 5; i++) {
            String hash = encoder.encode(plainPassword);
            System.out.println("  " + hash);
        }

        System.out.println("\n验证测试:");
        String hash = encoder.encode(plainPassword);
        System.out.println("  明文: " + plainPassword);
        System.out.println("  哈希: " + hash);
        System.out.println("  匹配: " + encoder.matches(plainPassword, hash));
        System.out.println("====================================");
    }

    /**
     * 验证两个哈希是否是同一密码
     */
    @Test
    public void testVerifyHash() {
        System.out.println("\n========== 哈希验证 ==========");

        // 数据库中的哈希
        String dbHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi";
        // 要验证的密码
        String testPassword = "123456";

        System.out.println("数据库哈希: " + dbHash);
        System.out.println("测试密码: [" + testPassword + "]");
        System.out.println("验证结果: " + encoder.matches(testPassword, dbHash));

        System.out.println("\n批量测试:");
        for (String pwd : Arrays.asList("123456", "admin123", "admin", "password")) {
            System.out.println("  " + pwd + " -> " + encoder.matches(pwd, dbHash));
        }
        System.out.println("===============================");
    }

    /**
     * 暴力破解密码
     */
    private String crackPassword(String hash) {
        for (String pwd : COMMON_PASSWORDS) {
            if (encoder.matches(pwd, hash)) {
                return pwd;
            }
        }
        return null;
    }

    /**
     * 修改用户密码到数据库
     * 修改下面的参数后运行此方法
     */
    @Test
    public void testUpdatePassword() {
        System.out.println("\n========== 修改用户密码 ==========");

        // ============ 修改这里的参数 ============
        String username = "changjiangqiye1";           // 要修改密码的用户名
        String newPassword = "qiye123";     // 新密码
        // ======================================

        System.out.println("用户名: " + username);
        System.out.println("新密码: " + newPassword);

        // 生成 BCrypt 哈希
        String hashedPassword = encoder.encode(newPassword);
        System.out.println("哈希值: " + hashedPassword);

        boolean updated = false;

        // 尝试更新管理员
        UserEnterprise admin = userEnterpriseMapper.findByUsername(username);
        if (admin != null) {
            admin.setPassword(hashedPassword);
            userEnterpriseMapper.updateById(admin);
            System.out.println("✓ 管理员密码已更新");
            updated = true;
        }

        // 尝试更新个人用户
        if (!updated) {
            UserPersonal personal = userPersonalMapper.findByUsername(username);
            if (personal != null) {
                personal.setPassword(hashedPassword);
                userPersonalMapper.updateById(personal);
                System.out.println("✓ 个人用户密码已更新");
                updated = true;
            }
        }

        // 尝试更新企业用户
        if (!updated) {
            UserEnterprise enterprise = userEnterpriseMapper.findByUsername(username);
            if (enterprise != null) {
                enterprise.setPassword(hashedPassword);
                userEnterpriseMapper.updateById(enterprise);
                System.out.println("✓ 企业用户密码已更新");
                updated = true;
            }
        }

        if (!updated) {
            System.out.println("✗ 未找到用户: " + username);
            System.out.println("==================================");
            return;
        }

        // 验证更新
        System.out.println("\n验证新密码:");
        if (userAdminMapper.findByUsername(username) != null) {
            boolean matches = encoder.matches(newPassword, userAdminMapper.findByUsername(username).getPassword());
            System.out.println("  密码验证: " + (matches ? "✓ 成功" : "✗ 失败"));
        }
        System.out.println("\n现在可以使用新密码登录了！");
        System.out.println("==================================");
    }

    /**
     * 批量修改所有用户密码为统一密码
     */
    @Test
    public void testResetAllPasswords() {
        System.out.println("\n========== 批量重置密码 ==========");

        String newPassword = "123456";  // 统一密码
        System.out.println("统一密码: " + newPassword);
        String hashedPassword = encoder.encode(newPassword);
        System.out.println("哈希值: " + hashedPassword);

        int count = 0;

        // 管理员
        List<UserAdmin> admins = userAdminMapper.selectList(null);
        for (UserAdmin admin : admins) {
            admin.setPassword(hashedPassword);
            userAdminMapper.updateById(admin);
            System.out.println("  管理员: " + admin.getUsername() + " -> ✓");
            count++;
        }

        // 个人用户
        List<UserPersonal> personalUsers = userPersonalMapper.selectList(null);
        for (UserPersonal user : personalUsers) {
            user.setPassword(hashedPassword);
            userPersonalMapper.updateById(user);
            System.out.println("  个人用户: " + user.getUsername() + " -> ✓");
            count++;
        }

        // 企业用户
        List<UserEnterprise> enterpriseUsers = userEnterpriseMapper.selectList(null);
        for (UserEnterprise user : enterpriseUsers) {
            user.setPassword(hashedPassword);
            userEnterpriseMapper.updateById(user);
            System.out.println("  企业用户: " + user.getUsername() + " -> ✓");
            count++;
        }

        System.out.println("\n✓ 共更新 " + count + " 个用户");
        System.out.println("所有用户密码已重置为: " + newPassword);
        System.out.println("==================================");
    }

    /**
     * 批量更新所有用户头像为卡通风格
     */
    @Test
    public void testUpdateAllAvatars() {
        System.out.println("\n========== 批量更新所有用户头像 ==========");

        int totalUpdated = 0;

        // 1. 更新管理员头像
        System.out.println("\n--- 管理员 ---");
        List<UserAdmin> admins = userAdminMapper.selectList(null);
        for (UserAdmin admin : admins) {
            String avatarUrl = avatarService.generateAvatarByUsername(admin.getUsername());
            admin.setAvatar(avatarUrl);
            userAdminMapper.updateById(admin);
            System.out.println("  " + admin.getUsername() + " -> " + avatarUrl);
            totalUpdated++;
        }

        // 2. 更新个人用户头像
        System.out.println("\n--- 个人用户 ---");
        List<UserPersonal> personalUsers = userPersonalMapper.selectList(null);
        for (UserPersonal user : personalUsers) {
            String avatarUrl = avatarService.generateAvatarByUsername(user.getUsername());
            user.setAvatar(avatarUrl);
            userPersonalMapper.updateById(user);
            System.out.println("  " + user.getUsername() + " -> " + avatarUrl);
            totalUpdated++;
        }

        // 3. 更新企业用户头像（使用卡通风格）
        System.out.println("\n--- 企业用户 ---");
        List<UserEnterprise> enterpriseUsers = userEnterpriseMapper.selectList(null);
        for (UserEnterprise user : enterpriseUsers) {
            // 使用用户名生成卡通头像
            String avatarUrl = avatarService.generateAvatarByUsername(user.getUsername());
            user.setLogo(avatarUrl);
            userEnterpriseMapper.updateById(user);
            System.out.println("  " + user.getUsername() + " -> " + avatarUrl);
            totalUpdated++;
        }

        System.out.println("\n========== 更新完成 ==========");
        System.out.println("✓ 共更新 " + totalUpdated + " 个用户的头像");
        System.out.println("所有用户头像已统一为卡通风格 (adventurer)");
        System.out.println("==================================");
    }
}
