package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-23 15:26
 **/
@Service
@SuppressWarnings("all")
public class CartAsyncService {
    @Autowired
    CartMapper cartMapper;

    @Async
    public void insert(Cart cart) {
        cartMapper.insert(cart);
    }

    @Async
    public void update(Cart cart, QueryWrapper<Cart> queryWrapper) {
        cartMapper.update(cart, queryWrapper);
    }

    @Async
    public void deleteByUserId(String userKey) {
        cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userKey));
    }

    @Async
    public void deleteCart(String userId, Long skuId) {
        cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
    }

    @Async
    public void chooseAll(String userId){
        Cart cart =new Cart();
        cart.setCheck(true);
        cartMapper.update(cart, new QueryWrapper<Cart>().eq("user_id",userId));
    }
}
