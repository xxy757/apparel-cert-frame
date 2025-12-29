package com.apparelcert.service.impl;

import com.apparelcert.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮件服务实现类
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@apparelcert.com}")
    private String fromEmail;

    // 简单的内存缓存存储验证码（生产环境应使用Redis）
    private final Map<String, VerifyCodeInfo> verifyCodeCache = new ConcurrentHashMap<>();

    // 验证码有效期（毫秒）
    private static final long CODE_EXPIRE_TIME = 5 * 60 * 1000; // 5分钟

    @Override
    public boolean sendSimpleMail(String to, String subject, String content) {
        try {
            if (mailSender == null) {
                logger.warn("邮件服务未配置，模拟发送邮件到: {}", to);
                return true;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            logger.info("简单邮件发送成功: {}", to);
            return true;
        } catch (Exception e) {
            logger.error("简单邮件发送失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendHtmlMail(String to, String subject, String htmlContent) {
        try {
            if (mailSender == null) {
                logger.warn("邮件服务未配置，模拟发送HTML邮件到: {}", to);
                return true;
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            logger.info("HTML邮件发送成功: {}", to);
            return true;
        } catch (MessagingException e) {
            logger.error("HTML邮件发送失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendRegisterVerifyCode(String to, String code) {
        String subject = "【服装行业人才认证平台】注册验证码";
        String htmlContent = buildVerifyCodeEmailTemplate("注册验证", code, "完成账号注册");
        saveVerifyCode(to, code, "register");
        return sendHtmlMail(to, subject, htmlContent);
    }

    @Override
    public boolean sendResetPasswordCode(String to, String code) {
        String subject = "【服装行业人才认证平台】密码重置验证码";
        String htmlContent = buildVerifyCodeEmailTemplate("密码重置", code, "重置您的密码");
        saveVerifyCode(to, code, "reset");
        return sendHtmlMail(to, subject, htmlContent);
    }

    @Override
    public boolean sendCertificationResultNotification(String to, String userName, String certificationName, boolean passed, String reason) {
        String subject = "【服装行业人才认证平台】认证结果通知";
        String htmlContent = buildCertificationResultTemplate(userName, certificationName, passed, reason);
        return sendHtmlMail(to, subject, htmlContent);
    }

    @Override
    public boolean sendInterviewInvitation(String to, String userName, String companyName, String jobTitle, String interviewTime, String location) {
        String subject = "【服装行业人才认证平台】面试邀请通知";
        String htmlContent = buildInterviewInvitationTemplate(userName, companyName, jobTitle, interviewTime, location);
        return sendHtmlMail(to, subject, htmlContent);
    }

    @Override
    public String generateVerifyCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    @Override
    public void saveVerifyCode(String email, String code, String type) {
        String key = type + ":" + email;
        verifyCodeCache.put(key, new VerifyCodeInfo(code, System.currentTimeMillis()));
        // 清理过期的验证码
        cleanExpiredCodes();
    }

    @Override
    public boolean verifyCode(String email, String code, String type) {
        String key = type + ":" + email;
        VerifyCodeInfo info = verifyCodeCache.get(key);
        if (info == null) {
            return false;
        }
        // 检查是否过期
        if (System.currentTimeMillis() - info.createTime > CODE_EXPIRE_TIME) {
            verifyCodeCache.remove(key);
            return false;
        }
        // 验证码匹配
        if (info.code.equals(code)) {
            verifyCodeCache.remove(key); // 验证成功后删除
            return true;
        }
        return false;
    }

    private void cleanExpiredCodes() {
        long now = System.currentTimeMillis();
        verifyCodeCache.entrySet().removeIf(entry -> 
            now - entry.getValue().createTime > CODE_EXPIRE_TIME);
    }

    private String buildVerifyCodeEmailTemplate(String action, String code, String purpose) {
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; padding: 20px;'>" +
            "<div style='max-width: 600px; margin: 0 auto; background: #f9f9f9; padding: 30px; border-radius: 10px;'>" +
            "<h2 style='color: #333; text-align: center;'>服装行业人才技能认证与招聘服务平台</h2>" +
            "<div style='background: #fff; padding: 20px; border-radius: 8px; margin-top: 20px;'>" +
            "<p style='color: #666;'>您好！</p>" +
            "<p style='color: #666;'>您正在进行<strong>" + action + "</strong>操作，验证码为：</p>" +
            "<div style='text-align: center; margin: 30px 0;'>" +
            "<span style='font-size: 32px; font-weight: bold; color: #409eff; letter-spacing: 8px; background: #ecf5ff; padding: 15px 30px; border-radius: 8px;'>" + code + "</span>" +
            "</div>" +
            "<p style='color: #999; font-size: 14px;'>验证码有效期为5分钟，请尽快" + purpose + "。</p>" +
            "<p style='color: #999; font-size: 14px;'>如非本人操作，请忽略此邮件。</p>" +
            "</div>" +
            "<p style='color: #999; font-size: 12px; text-align: center; margin-top: 20px;'>此邮件由系统自动发送，请勿回复</p>" +
            "</div></body></html>";
    }

    private String buildCertificationResultTemplate(String userName, String certificationName, boolean passed, String reason) {
        String resultText = passed ? "恭喜您通过认证！" : "很遗憾，您未通过认证。";
        String resultColor = passed ? "#67c23a" : "#f56c6c";
        String reasonHtml = passed ? "" : "<p style='color: #666;'>原因：" + reason + "</p>";
        
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; padding: 20px;'>" +
            "<div style='max-width: 600px; margin: 0 auto; background: #f9f9f9; padding: 30px; border-radius: 10px;'>" +
            "<h2 style='color: #333; text-align: center;'>认证结果通知</h2>" +
            "<div style='background: #fff; padding: 20px; border-radius: 8px; margin-top: 20px;'>" +
            "<p style='color: #666;'>尊敬的 " + userName + "：</p>" +
            "<p style='color: #666;'>您申请的<strong>「" + certificationName + "」</strong>认证已完成审核。</p>" +
            "<div style='text-align: center; margin: 30px 0;'>" +
            "<span style='font-size: 24px; font-weight: bold; color: " + resultColor + ";'>" + resultText + "</span>" +
            "</div>" +
            reasonHtml +
            "<p style='color: #666;'>如有疑问，请登录平台查看详情或联系客服。</p>" +
            "</div></div></body></html>";
    }

    private String buildInterviewInvitationTemplate(String userName, String companyName, String jobTitle, String interviewTime, String location) {
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'></head><body style='font-family: Arial, sans-serif; padding: 20px;'>" +
            "<div style='max-width: 600px; margin: 0 auto; background: #f9f9f9; padding: 30px; border-radius: 10px;'>" +
            "<h2 style='color: #333; text-align: center;'>面试邀请通知</h2>" +
            "<div style='background: #fff; padding: 20px; border-radius: 8px; margin-top: 20px;'>" +
            "<p style='color: #666;'>尊敬的 " + userName + "：</p>" +
            "<p style='color: #666;'>恭喜您！<strong>" + companyName + "</strong> 对您的简历非常感兴趣，诚邀您参加面试。</p>" +
            "<div style='background: #ecf5ff; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
            "<p style='margin: 8px 0;'><strong>应聘岗位：</strong>" + jobTitle + "</p>" +
            "<p style='margin: 8px 0;'><strong>面试时间：</strong>" + interviewTime + "</p>" +
            "<p style='margin: 8px 0;'><strong>面试地点：</strong>" + location + "</p>" +
            "</div>" +
            "<p style='color: #666;'>请准时参加面试，如有变动请提前联系企业HR。</p>" +
            "<p style='color: #666;'>祝您面试顺利！</p>" +
            "</div></div></body></html>";
    }

    // 验证码信息内部类
    private static class VerifyCodeInfo {
        String code;
        long createTime;

        VerifyCodeInfo(String code, long createTime) {
            this.code = code;
            this.createTime = createTime;
        }
    }
}
