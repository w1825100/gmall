package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @program: gmall
 * @description: 定时关单监听器
 * @author: lgd
 * @create: 2021-02-26 19:08
 **/
@Component
@Slf4j
@SuppressWarnings("all")
public class ShutDownOrderListener {

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    OrderMapper orderMapper;



    @RabbitListener(queues = "ORDER_SHUT_DOWN")
    public void shutDownOrder(String orderToken, Channel channel, Message message) throws IOException {
        log.warn("oms关单队列监听到消息了");
        if (StringUtils.isBlank(orderToken)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return ;
        }

        // 更新订单状态为关闭订单
        if (this.orderMapper.updateStatus(orderToken, 4, 0) == 1) {
            log.warn("oms微服务更新了订单状态,orderSn:{},Status:{}",orderToken,4);
            // 发送消息给wms解锁库存
            this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.unlock", orderToken);
            log.warn("oms微服务给wms微服务发送解锁库存消息,orderSn:{},Status:{}",orderToken,4);
        }

        // 最后确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
