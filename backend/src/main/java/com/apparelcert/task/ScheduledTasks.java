package com.apparelcert.task;

import com.apparelcert.entity.Certificate;
import com.apparelcert.entity.ScheduledDelivery;
import com.apparelcert.service.CertificateService;
import com.apparelcert.service.EmailService;
import com.apparelcert.service.ScheduledDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 定时任务执行器
 */
@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired(required = false)
    private ScheduledDeliveryService scheduledDeliveryService;
    
    @Autowired(required = false)
    private CertificateService certificateService;
    
    @Autowired(required = false)
    private EmailService emailService;

    /**
     * 执行定时投递任务
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void executeScheduledDeliveries() {
        if (scheduledDeliveryService == null) {
            return;
        }
        
        try {
            List<ScheduledDelivery> pendingDeliveries = scheduledDeliveryService.getPendingDeliveries();
            
            if (!pendingDeliveries.isEmpty()) {
                logger.info("开始执行定时投递任务，待执行数量: {}", pendingDeliveries.size());
                
                for (ScheduledDelivery delivery : pendingDeliveries) {
                    scheduledDeliveryService.executeScheduledDelivery(delivery);
                }
                
                logger.info("定时投递任务执行完成");
            }
        } catch (Exception e) {
            logger.error("定时投递任务执行异常: {}", e.getMessage());
        }
    }

    /**
     * 证书过期提醒任务
     * 每天早上9点执行
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendCertificateExpiryReminders() {
        if (certificateService == null || emailService == null) {
            return;
        }
        
        try {
            logger.info("开始执行证书过期提醒任务");
            
            // 获取30天内即将过期的证书
            List<Certificate> expiringCertificates = certificateService.getExpiringCertificates(null, 30);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            for (Certificate cert : expiringCertificates) {
                try {
                    // 这里需要获取用户邮箱，简化处理
                    String subject = "【证书过期提醒】您的技能认证证书即将过期";
                    String content = buildExpiryReminderContent(cert, sdf);
                    
                    // 发送提醒邮件（需要用户邮箱，这里仅记录日志）
                    logger.info("证书过期提醒: certificateId={}, certificateNumber={}, expireDate={}", 
                            cert.getId(), cert.getCertificateNumber(), sdf.format(cert.getExpireDate()));
                    
                } catch (Exception e) {
                    logger.error("发送证书过期提醒失败: certificateId={}, error={}", cert.getId(), e.getMessage());
                }
            }
            
            logger.info("证书过期提醒任务执行完成，处理证书数量: {}", expiringCertificates.size());
            
        } catch (Exception e) {
            logger.error("证书过期提醒任务执行异常: {}", e.getMessage());
        }
    }

    /**
     * 构建过期提醒邮件内容
     */
    private String buildExpiryReminderContent(Certificate cert, SimpleDateFormat sdf) {
        StringBuilder sb = new StringBuilder();
        sb.append("尊敬的 ").append(cert.getName()).append("：\n\n");
        sb.append("您的技能认证证书即将过期，请及时续期。\n\n");
        sb.append("证书信息：\n");
        sb.append("- 证书编号：").append(cert.getCertificateNumber()).append("\n");
        sb.append("- 认证类型：").append(cert.getCertificationType()).append("\n");
        sb.append("- 过期日期：").append(sdf.format(cert.getExpireDate())).append("\n\n");
        sb.append("请登录平台进行证书续期操作。\n\n");
        sb.append("服装行业人才技能认证中心");
        return sb.toString();
    }

    /**
     * 清理过期的定时投递任务
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredScheduledDeliveries() {
        logger.info("开始清理过期的定时投递任务");
        // 可以添加清理逻辑，如删除30天前已完成的任务
        logger.info("清理任务执行完成");
    }
}
