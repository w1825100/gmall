package com.atguigu.gmall.wms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-28 08:46
 **/
@Configuration
public class RabbitConfig {

    @Bean
    public Queue unLockWareQueue(){

        return new Queue("ORDER_UNLOCK_QUEUE",true,false,false,null);

    }


    @Bean
    public Binding binding(){
        return new Binding("ORDER_UNLOCK_QUEUE", Binding.DestinationType.QUEUE,
                "ORDER_EXCHANGE","stock.unlock",null);
    }

    //定时解锁库存的延时队列
    @Bean
    public Queue ttlUnlockWare(){
       Map<String,Object> map=new HashMap<>();
       map.put("x-message-ttl", 1000000);
       map.put("x-dead-letter-exchange", "ORDER_EXCHANGE");
       map.put("x-dead-letter-routing-key", "stock.unlock");
        return new Queue("STOCK_TTL_QUEUE",true,false,false,map);
    }


    @Bean
    public Binding bindingTtl(){
        return new Binding("STOCK_TTL_QUEUE", Binding.DestinationType.QUEUE,
                "ORDER_EXCHANGE","stock.ttl",null);
    }

}
