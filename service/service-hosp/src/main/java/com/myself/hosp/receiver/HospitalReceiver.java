package com.myself.hosp.receiver;

import com.myself.hosp.service.ScheduleService;
import com.myself.model.hosp.Schedule;
import com.myself.rabbitmq.constant.RabbitMQConst;
import com.myself.rabbitmq.service.RabbitMQService;
import com.myself.vo.msm.MsmVo;
import com.myself.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息监听器
 *
 * @author Wei
 * @since 2021/7/9
 */
@Component
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private RabbitMQService rabbitMQService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = RabbitMQConst.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = RabbitMQConst.EXCHANGE_DIRECT_ORDER),
            key = {RabbitMQConst.ROUTING_ORDER}
    ))
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel) {
        if (null != orderMqVo.getAvailableNumber()) {
            //下单成功更新预约数
            Schedule schedule = scheduleService.getSchedule(orderMqVo.getScheduleId());
            schedule.setReservedNumber(orderMqVo.getReservedNumber());
            schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
            scheduleService.update(schedule);
        } else {
            //取消预约更新预约数
            Schedule schedule = scheduleService.getSchedule(orderMqVo.getScheduleId());
            int availableNumber = schedule.getAvailableNumber().intValue() + 1;
            schedule.setAvailableNumber(availableNumber);
            scheduleService.update(schedule);
        }
        //发送短信
        MsmVo msmVo = orderMqVo.getMsmVo();
        if (null != msmVo) {
            rabbitMQService.sendMessage(RabbitMQConst.EXCHANGE_DIRECT_MSM, RabbitMQConst.ROUTING_MSM_ITEM, msmVo);
        }
    }

}
