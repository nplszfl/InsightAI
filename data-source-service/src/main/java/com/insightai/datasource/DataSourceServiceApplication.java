package com.insightai.datasource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DataSourceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataSourceServiceApplication.class, args);
    }
}
