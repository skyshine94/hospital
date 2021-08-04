package com.myself.order.service;

import java.util.Map;

public interface WeixinService {

    Map<String, String> createNative(Long orderId);

    Map<String, String> queryPayStatus(Long orderId);

    Boolean refund(Long orderId);
}
