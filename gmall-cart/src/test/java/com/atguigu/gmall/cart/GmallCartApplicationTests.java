package com.atguigu.gmall.cart;

import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallCartApplicationTests {

    @Autowired
    CartMapper cartMapper;
    @Autowired
    CartService cartService;

    @Test
    void contextLoads() {
        System.out.println(cartMapper.getClass());
        System.out.println(cartService.getClass());
    }

}
