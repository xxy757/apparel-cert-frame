package com.apparelcert.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 开发模式重置密码配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "apparelcert.reset-pwd")
public class ResetPwdProperties {

    /**
     * 是否启用开发模式（默认false）
     */
    private boolean devMode = false;

    /**
     * 允许访问的CIDR列表（内网IP）
     */
    private List<String> allowCidrs;
}
