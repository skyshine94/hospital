package com.myself.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myself.model.order.PaymentInfo;
import com.myself.model.order.RefundInfo;

public interface RefundInfoService extends IService<RefundInfo> {

    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);
}
