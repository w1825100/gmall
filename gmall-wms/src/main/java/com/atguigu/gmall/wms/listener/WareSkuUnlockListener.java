package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.util.List;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-28 08:45
 **/
@Component
@Slf4j
@SuppressWarnings("all")
public class WareSkuUnlockListener {
    private static final String LOCK_PREFIX = "gmall:stock:lock";
    @Autowired
    WareSkuMapper wareSkuMapper;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    StringRedisTemplate redisTemplate;

    @RabbitListener(queues = "ORDER_UNLOCK_QUEUE")
    public void unlock(String orderToken, Channel channel, Message message) {
        try {
            if (StringUtils.isBlank(orderToken)){
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return ;
            }
            // 获取redis中缓存的锁定库存信息
            String json = this.redisTemplate.opsForValue().get(LOCK_PREFIX + orderToken);
            if (StringUtils.isBlank(json)){ // 如果缓存的锁定库存信息为空，直接消费掉消息
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return ;
            }
            // 反序列化锁定库存信息的集合
            List<SkuLockVo> skuLockVos = JSON.parseArray(json, SkuLockVo.class);
            if (CollectionUtils.isEmpty(skuLockVos)){
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return ;
            }
            // 遍历集合解锁库存信息
            skuLockVos.forEach(lockVo -> {
                this.wareSkuMapper.unlock(lockVo.getWareSkuId(), lockVo.getCount());
                log.warn("wms微服务解锁了库存:wareSkuId:{},数量:{}",lockVo.getWareSkuId(), lockVo.getCount());
            });

            // 删除锁定库存的缓存
            this.redisTemplate.delete(LOCK_PREFIX + orderToken);
            log.warn("wms微服务删除了锁定库存的缓存:{}",orderToken);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.warn("wms解锁库存确认消息失败:{}",e);

            e.printStackTrace();
        }

    }
}
