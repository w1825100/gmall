package com.atguigu.gmall.order;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallOrderApplicationTests {

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Test
    void contextLoads() {

        rabbitTemplate.convertAndSend("ORDER_EXCHANGE","order.create","呵呵");
    }

    public static void main(String[] args) {
        System.out.println(Integer.toBinaryString(10));

    }
}
