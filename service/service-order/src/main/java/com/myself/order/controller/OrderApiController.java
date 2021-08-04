package com.myself.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myself.common.result.Result;
import com.myself.common.utils.AuthContextHolder;
import com.myself.enums.OrderStatusEnum;
import com.myself.model.order.OrderInfo;
import com.myself.order.service.OrderService;
import com.myself.vo.order.OrderCountQueryVo;
import com.myself.vo.order.OrderQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 订单模块
 *
 * @author Wei
 * @since 2021/7/9
 */
@Api(tags = "订单管理")
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderApiController {

    @Autowired
    private OrderService orderService;


    @ApiOperation(value = "创建订单")
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public Result submitOrder(@PathVariable String scheduleId, @PathVariable Long patientId) {
        Long orderId = orderService.submitOrder(scheduleId, patientId);
        return Result.ok(orderId);
    }

    @ApiOperation(value = "根据orderId查询订单详情")
    @GetMapping("auth/getOrder/{orderId}")
    public Result getOrder(@PathVariable String orderId) {
        OrderInfo orderInfo = orderService.getOrder(orderId);
        return Result.ok(orderInfo);
    }

    @ApiOperation(value = "分页带条件查询订单列表")
    @GetMapping("auth/{page}/{limit}")
    public Result list(@PathVariable Long page, @PathVariable Long limit, OrderQueryVo orderQueryVo, HttpServletRequest request) {
        orderQueryVo.setUserId(AuthContextHolder.getUserId(request));
        Page<OrderInfo> pageParam = new Page(page, limit);
        IPage<OrderInfo> pageModel = orderService.selectPage(pageParam, orderQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "获取所有订单状态")
    @GetMapping("auth/getStatusList")
    public Result getStatusList() {
        List<Map<String, Object>> statusList = OrderStatusEnum.getStatusList();
        return Result.ok(statusList);
    }

    @ApiOperation(value = "取消预约")
    @GetMapping("auth/cancelOrder/{orderId}")
    public Result cancelOrder(@PathVariable Long orderId) {
        Boolean isOrder = orderService.cancelOrder(orderId);
        return Result.ok(isOrder);
    }

    @ApiOperation(value = "预约统计")
    @PostMapping("inner/getCountMap")
    public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo) {
        Map<String, Object> countMap = orderService.getCountMap(orderCountQueryVo);
        return countMap;
    }

}
