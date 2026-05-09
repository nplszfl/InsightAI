package com.insightai.common;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CommonApplication {
    public static void main(String[] args) {
        System.out.println("Common Module - Library only, not runnable");
    }
}
