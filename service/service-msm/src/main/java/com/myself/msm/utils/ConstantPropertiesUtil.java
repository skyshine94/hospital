package com.myself.msm.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 常量初始化工具类
 *
 * @author Wei
 * @since 2021/7/5
 */
@Component
public class ConstantPropertiesUtil implements InitializingBean {

    @Value("${aliyun.sms.regionId}")
    private String regionId;

    @Value("${aliyun.sms.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.sms.secret}")
    private String secret;

    public static String REGION_ID;
    public static String ACCESS_KEY_ID;
    public static String SECRET;

    //在项目启动时，从配置文件中初始化常量值
    @Override
    public void afterPropertiesSet() throws Exception {
        REGION_ID = regionId;
        ACCESS_KEY_ID = accessKeyId;
        SECRET = secret;
    }

}
