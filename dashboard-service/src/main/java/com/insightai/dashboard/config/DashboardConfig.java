package com.insightai.dashboard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightai.dashboard.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DashboardConfig {

    @Bean
    public ObjectMapper dashboardObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public DashboardService dashboardService(ObjectMapper mapper) {
        return new DashboardService();
    }
}
