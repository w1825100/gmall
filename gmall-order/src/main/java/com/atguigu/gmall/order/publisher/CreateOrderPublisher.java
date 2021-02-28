package com.atguigu.gmall.order.publisher;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-26 18:23
 **/
@Configuration
public class CreateOrderPublisher {
    //创建所需交换机 队列 并指定绑定关系

    @Bean
    public Exchange orderExchange() {
        return new TopicExchange("ORDER_EXCHANGE",true,false);
    }

    @Bean
    public Queue orderDelayQueue(){
        HashMap<String, Object> argsMap = new HashMap<>();
        argsMap.put("x-message-ttl",90000);
        argsMap.put("x-dead-letter-exchange", "ORDER_EXCHANGE");
        argsMap.put("x-dead-letter-routing-key","order.dead");
        Queue order_delay = new Queue("ORDER_DELAY", true, false, false,argsMap);
        return order_delay;
    }

    @Bean
    public Queue shutDownQueue(){
    return new Queue("ORDER_SHUT_DOWN",true,false,false);
    }

    @Bean
    public Binding orderDelayBinding(){
        return new Binding("ORDER_DELAY",
                Binding.DestinationType.QUEUE,
                "ORDER_EXCHANGE","order.create",null);
    }

    @Bean
    public  Binding orderShutDownBinding(){
        return new Binding("ORDER_SHUT_DOWN",
                Binding.DestinationType.QUEUE,
                "ORDER_EXCHANGE","order.dead",null);

    }
}
