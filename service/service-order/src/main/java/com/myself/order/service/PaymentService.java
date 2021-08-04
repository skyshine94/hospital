package com.myself.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myself.model.order.OrderInfo;
import com.myself.model.order.PaymentInfo;

import java.util.Map;

public interface PaymentService extends IService<PaymentInfo> {

    void savePaymentInfo(OrderInfo orderInfo, Integer paymentType);

    void paySuccess(Map<String, String> map);

    PaymentInfo getPaymentInfo(Long orderId, Integer paymentType);
}
