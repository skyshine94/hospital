package com.myself.task.schedule;

import com.myself.rabbitmq.constant.RabbitMQConst;
import com.myself.rabbitmq.service.RabbitMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务类
 *
 * @author Wei
 * @since 2021/7/11
 */
@Component
@EnableScheduling
public class ScheduleTask {

    @Autowired
    private RabbitMQService rabbitMQService;

    //每天八点执行就医提醒方法
    @Scheduled(cron = "0 0 8 * * ?")
    //@Scheduled(cron = "0/30 * * * * ?") //cron表达式，设置执行间隔
    public void taskPatient(){
        rabbitMQService.sendMessage(RabbitMQConst.EXCHANGE_DIRECT_TASK, RabbitMQConst.ROUTING_TASK_8, "");
    }
}
