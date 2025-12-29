package com.apparelcert.service;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送简单文本邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @return 是否发送成功
     */
    boolean sendSimpleMail(String to, String subject, String content);

    /**
     * 发送HTML邮件
     * @param to 收件人
     * @param subject 主题
     * @param htmlContent HTML内容
     * @return 是否发送成功
     */
    boolean sendHtmlMail(String to, String subject, String htmlContent);

    /**
     * 发送注册验证码邮件
     * @param to 收件人邮箱
     * @param code 验证码
     * @return 是否发送成功
     */
    boolean sendRegisterVerifyCode(String to, String code);

    /**
     * 发送找回密码验证码邮件
     * @param to 收件人邮箱
     * @param code 验证码
     * @return 是否发送成功
     */
    boolean sendResetPasswordCode(String to, String code);

    /**
     * 发送认证结果通知邮件
     * @param to 收件人邮箱
     * @param userName 用户姓名
     * @param certificationName 认证名称
     * @param passed 是否通过
     * @param reason 原因（不通过时）
     * @return 是否发送成功
     */
    boolean sendCertificationResultNotification(String to, String userName, String certificationName, boolean passed, String reason);

    /**
     * 发送面试邀请邮件
     * @param to 收件人邮箱
     * @param userName 用户姓名
     * @param companyName 企业名称
     * @param jobTitle 岗位名称
     * @param interviewTime 面试时间
     * @param location 面试地点
     * @return 是否发送成功
     */
    boolean sendInterviewInvitation(String to, String userName, String companyName, String jobTitle, String interviewTime, String location);

    /**
     * 生成验证码
     * @return 6位数字验证码
     */
    String generateVerifyCode();

    /**
     * 保存验证码到缓存
     * @param email 邮箱
     * @param code 验证码
     * @param type 类型（register/reset）
     */
    void saveVerifyCode(String email, String code, String type);

    /**
     * 验证验证码
     * @param email 邮箱
     * @param code 验证码
     * @param type 类型
     * @return 是否有效
     */
    boolean verifyCode(String email, String code, String type);
}
