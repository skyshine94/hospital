package com.myself.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 订单模块启动入口类
 *
 * @author Wei
 * @since 2021/7/9
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.myself")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.myself")
public class ServiceOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOrderApplication.class, args);
    }
}
