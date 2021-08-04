package com.myself.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户登录模块启动入口类
 * @author Wei
 * @since 2021/7/3
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.myself")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.myself")
public class ServiceUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceUserApplication.class, args);
    }
}
