package com.myself.hosp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 医院设置模块启动入口类
 *
 * @author Wei
 * @since 2021/6/17
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.myself")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.myself")
public class ServiceHospApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceHospApplication.class, args);
    }
}
