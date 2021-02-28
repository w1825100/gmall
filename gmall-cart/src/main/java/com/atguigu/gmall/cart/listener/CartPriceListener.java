package com.atguigu.gmall.cart.listener;

import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-23 19:56
 **/
@Component
@Slf4j
public class CartPriceListener {
    @Autowired
    GmallPmsClient pmsClient;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    private static final String CART_PRICE="gmall:cart:price:";

    @RabbitListener(bindings = {
            @QueueBinding(
            value = @Queue(value = "CART_PRICE_QUEUE",durable = "true"),
            exchange = @Exchange(value = "PMS_ITEM_EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = "item.update"
            )}
    )
    public void listener(Long spuId, Channel channel, Message message){
        log.warn("购物车价格同步监听到mq消息了,spuId:{}", spuId);
        try {
            ResponseVo<List<SkuEntity>> resp = pmsClient.querySkusBySpuId(spuId);
            List<SkuEntity> skus = resp.getData();
            if(CollectionUtils.isEmpty(skus)){
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            skus.forEach(sku->{
                String key=CART_PRICE+sku.getId();
                if(stringRedisTemplate.hasKey(key)){
                    log.warn("同步sku价格:skuId={},skuPrice={}",sku.getId(),sku.getPrice());
                    stringRedisTemplate.opsForValue().set(key,sku.getPrice().toString());
                }
            });
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.warn("购物车监听器异常:{}",e.getMessage());
        }
    }
}
