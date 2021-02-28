package com.atguigu.gmall.cart.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.consts.CartConstants;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-27 16:37
 **/
@Component
@Slf4j
public class CartDelListener {

    @Autowired
    StringRedisTemplate redisTemplate;

    @RabbitListener(queues = "CART_DELETE_QUEUE")
    public void listener(Map<String,Object> map, Channel channel, Message message){
        String userId= null;
        String skuIdJsons= null;
        try {
            userId = map.get("userId").toString();
            skuIdJsons = map.get("skuIds").toString();
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(CartConstants.CART_PREFIX + userId);
            List<String> idList = JSON.parseArray(skuIdJsons, String.class);
            hashOps.delete(idList.toArray());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            log.error("删除购物车出现异常,{}",e);
            e.printStackTrace();
        }
        log.warn("购物车微服务监听到删除购物车消息了userId:{},skuId:{}",userId,skuIdJsons);

    }

}
