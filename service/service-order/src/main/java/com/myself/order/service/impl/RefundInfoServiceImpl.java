package com.myself.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myself.enums.RefundStatusEnum;
import com.myself.model.order.PaymentInfo;
import com.myself.model.order.RefundInfo;
import com.myself.order.mapper.RefundInfoMapper;
import com.myself.order.service.RefundInfoService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {

    //添加退款信息
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        //判断是否已经存在退款信息
        RefundInfo refundInfo = baseMapper.selectOne(new QueryWrapper<RefundInfo>()
                .eq("order_id", paymentInfo.getOrderId())
                .eq("payment_type", paymentInfo.getPaymentType())
        );
        if (null != refundInfo) {
            return refundInfo;
        }

        //添加退款信息
        refundInfo = new RefundInfo();
        refundInfo.setCreateTime(new Date());
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setPaymentType(paymentInfo.getPaymentType());
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        refundInfo.setSubject(paymentInfo.getSubject());
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        baseMapper.insert(refundInfo);
        return refundInfo;
    }
}
