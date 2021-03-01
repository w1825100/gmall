package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-03-01 17:42
 **/
@Component
@Slf4j
public class SuccessOrderListenner {

    @Autowired
    OrderMapper orderMapper;
    @Autowired
    RabbitTemplate rabbitTemplate;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "OMS_SUCCESS_QUEUE", durable = "true"),
            exchange = @Exchange(value = "ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"order.success"}
    ))
    public void successOrder(String orderToken, Message message, Channel channel) throws IOException {
        if (StringUtils.isBlank(orderToken)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return ;
        }

        // 更新订单状态为成功
        if (this.orderMapper.updateStatus(orderToken, 1, 0) == 1) {
            log.warn("oms订单更新成功,给wms发消息减少库存..");
            // 发送消息给wms减库存
            this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.minus", orderToken);
            // TODO：发送消息给用户加积分userId integration growth

        }

        // 最后确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


}
