package com.atguigu.gmall.msg.listener;

import com.atguigu.gmall.msg.service.MsgService;
import com.atguigu.gmall.msg.utils.FormUtils;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * @program: gmall
 * @description: 短信队列消费方
 * @author: lgd
 * @create: 2021-02-03 21:05
 **/
@Component
public class MsgListener {

    @Autowired
    MsgService msgService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "MALL_MSG_QUEUE", durable = "true"),
            exchange = @Exchange(value = "PMS_MSG_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"msg.send"}
    ))
    public void listener(String mobile, Channel channel, Message message) throws IOException {
        if (mobile == null|| !FormUtils.isMobile(mobile)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
        Boolean send = msgService.send(mobile);
        if(send){
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

}
