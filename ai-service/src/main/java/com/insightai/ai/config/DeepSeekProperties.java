package com.insightai.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek.api")
public class DeepSeekProperties {
    private String url;
    private String model;
    private String apiKey;
    private long timeout;
    private int maxRetries;
    private long retryDelay;
}