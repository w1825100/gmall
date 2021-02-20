package com.atguigu.gmall.ums.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;

/**
 * @program: gmall
 * @description: rabbit发布消息回调
 * @author: lgd
 * @create: 2021-02-03 20:55
 **/
@Configuration
@Slf4j
public class RabbitConfig {


    @Autowired
    RabbitTemplate rabbitTemplate;
    @PostConstruct
    public  void  init(){
        rabbitTemplate.setConfirmCallback((@Nullable CorrelationData correlationData, boolean ack, @Nullable String cause)->{
            if(!ack){
                log.error("消息没有到达交换机:{}",cause);
            }
        });
        rabbitTemplate.setReturnCallback((Message message, int replyCode, String replyText, String exchange, String routingKey)->{
            log.error("消息没有到达队列消息内容:{},交换机:{},routingKey:{}",message,exchange,routingKey);
        });
    }
}
