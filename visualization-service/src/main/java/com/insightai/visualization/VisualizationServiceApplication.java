package com.insightai.visualization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class VisualizationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VisualizationServiceApplication.class, args);
    }
}
