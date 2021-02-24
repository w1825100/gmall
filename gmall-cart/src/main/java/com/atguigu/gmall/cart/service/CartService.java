package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.pojo.Cart;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-22 09:39
 **/
public interface CartService  extends IService<Cart> {
    void addCart(Cart cart);

    Cart queryCartBySkuId(Long skuId);
   /* void addCart2(Cart cart);
    Cart queryCartBySkuId2(Long skuId);*/

    List<Cart> queryCarts();

    void updateNum(Cart cart);

    void deleteCart(Long skuId);

    List<Cart> queryCheckedCartsByUserId(Long userId);

    void updateStatus(Cart cart);

    void chooseAll();
}
