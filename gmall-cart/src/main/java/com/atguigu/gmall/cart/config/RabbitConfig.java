package com.atguigu.gmall.cart.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-27 16:54
 **/
@Configuration
public class RabbitConfig {

    @Bean
    public Queue delQueue(){
        return  new Queue("CART_DELETE_QUEUE",true,false,false,null);
    }

    @Bean
    public Binding binding(){
        return new Binding("CART_DELETE_QUEUE",
                Binding.DestinationType.QUEUE,
                "ORDER_EXCHANGE","cart.delete",null);
    }
}
