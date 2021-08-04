package com.myself.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myself.enums.OrderStatusEnum;
import com.myself.enums.PaymentStatusEnum;
import com.myself.enums.PaymentTypeEnum;
import com.myself.hosp.client.HospitalFeignClient;
import com.myself.model.order.OrderInfo;
import com.myself.model.order.PaymentInfo;
import com.myself.order.mapper.PaymentMapper;
import com.myself.order.service.OrderService;
import com.myself.order.service.PaymentService;
import com.myself.service.helper.HttpRequestHelper;
import com.myself.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {

    @Autowired
    private OrderService orderService;
    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    //添加支付数据
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        //根据orderId和status查询订单是否存在
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper();
        wrapper.eq("order_id", orderInfo.getId());
        wrapper.eq("payment_type", paymentType);
        Integer count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            return;
        }
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + "|"
                + orderInfo.getHosname() + "|"
                + orderInfo.getDepname() + "|"
                + orderInfo.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(orderInfo.getAmount());
        baseMapper.insert(paymentInfo);
        return;
    }

    //更新订单状态
    @Override
    public void paySuccess(Map<String, String> map) {
        //获取支付信息
        String out_trade_no = map.get("out_trade_no");
        PaymentInfo paymentInfo = baseMapper.selectOne(new QueryWrapper<PaymentInfo>()
                .eq("out_trade_no", out_trade_no)
                .eq("payment_type", PaymentTypeEnum.WEIXIN.getStatus()));
        //更新支付信息
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setTradeNo(map.get("transaction_id"));
        paymentInfo.setCallbackContent(map.toString());
        baseMapper.updateById(paymentInfo);

        //根据orderId获取订单信息
        OrderInfo orderInfo = orderService.getById(paymentInfo.getOrderId());
        //更新订单信息
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderService.updateById(orderInfo);

        //调用医院接口，更新订单支付信息
        //获取医院签名
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        Map<String, Object> resultMap = new HashMap();
        resultMap.put("hoscode", orderInfo.getHoscode());
        resultMap.put("hosRecordId", orderInfo.getHosRecordId());
        resultMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(resultMap, signInfoVo.getSignKey());
        resultMap.put("sign", sign);
        //发送请求
        HttpRequestHelper.sendRequest(resultMap, signInfoVo.getApiUrl() + "/order/updatePayStatus");
    }

    //获取支付信息
    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        PaymentInfo paymentInfo = baseMapper.selectOne(new QueryWrapper<PaymentInfo>()
                .eq("order_id", orderId)
                .eq("payment_type", paymentType)
        );
        return paymentInfo;
    }
}
