package org.example.rentingmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 基于微信小程序的小区房屋租赁管理平台后端应用程序
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class HouserentBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HouserentBackendApplication.class, args);
    }

}