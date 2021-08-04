package com.myself.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.myself.model.order.OrderInfo;
import com.myself.vo.order.OrderCountQueryVo;
import com.myself.vo.order.OrderCountVo;
import com.myself.vo.order.OrderQueryVo;

import java.util.Map;

public interface OrderService extends IService<OrderInfo> {

    Long submitOrder(String scheduleId, Long patientId);

    OrderInfo getOrder(String orderId);

    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);

    Boolean cancelOrder(Long orderId);

    void patientTips();

    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);
}
