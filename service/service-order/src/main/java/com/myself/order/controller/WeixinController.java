package com.myself.order.controller;

import com.myself.common.result.Result;
import com.myself.order.service.PaymentService;
import com.myself.order.service.WeixinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 订单模块
 *
 * @author Wei
 * @since 2021/7/11
 */
@Api(tags = "微信支付")
@RestController
@RequestMapping("/api/order/weixin/")
public class WeixinController {

    @Autowired
    private WeixinService weixinService;
    @Autowired
    private PaymentService paymentService;

    @ApiOperation(value = "生成微信支付二维码")
    @GetMapping("createNative/{orderId}")
    public Result createNative(@PathVariable Long orderId) {
        Map<String, String> map = weixinService.createNative(orderId);
        return Result.ok(map);
    }

    @ApiOperation(value = "查询支付状态")
    @GetMapping("queryPayStatus/{orderId}")
    public Result queryPayStatus(@PathVariable Long orderId) {
        Map<String, String> map = weixinService.queryPayStatus(orderId);
        if(null == map){
            return Result.fail().message("支付失败");
        }
        if ("SUCCESS".equals(map.get("trade_state"))){
            //更新订单状态
            paymentService.paySuccess(map);
            return Result.ok().message("支付成功");
        }
        return Result.ok().message("支付中");
    }


}
