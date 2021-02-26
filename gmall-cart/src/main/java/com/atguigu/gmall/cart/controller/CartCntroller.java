package com.atguigu.gmall.cart.controller;

import ch.qos.logback.core.pattern.util.RegularEscapeUtil;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.interceptor.AuthHandler;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.concurrent.Future;


/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-21 13:45
 **/
@Controller
public class CartCntroller {



    @Autowired
   CartService cartService;



    //加入购物车前置方法,skuId,count
    @ApiOperation("新增购物车")
    @GetMapping("addToCart")
    public String addToCart(Cart cart) {
        cartService.addCart(cart);
        return "redirect:" + "http://cart.gmall.com/addToCart.html?skuId="+cart.getSkuId();

    }

    @ApiOperation("新增购物车回调")
    @GetMapping("addToCart.html")
    public ModelAndView addCart(@RequestParam Long skuId, ModelAndView model){
       Cart cart=cartService.queryCartBySkuId(skuId);
        model.addObject("cart",cart);
        model.setViewName("addCart");
        return model;
    }

    @ApiOperation("根据userKey查询购物车集合")
    @GetMapping("cart.html")
    public ModelAndView queryCart(ModelAndView modelAndView){
      List<Cart> carts= cartService.queryCarts();
        modelAndView.addObject("carts",carts);
        modelAndView.setViewName("cart");
        return modelAndView;
    }

    @ApiOperation("根据skuId更新选中状态")
    @ResponseBody
    @PostMapping("updateStatus")
    public ResponseVo updateStatus(@RequestBody Cart cart){
        cartService.updateStatus(cart);
        return ResponseVo.ok();
    }

    @ApiOperation("更新购物车数量")
    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo updateNum(@RequestBody Cart cart){
        this.cartService.updateNum(cart);
        return ResponseVo.ok();
    }

    @ApiOperation("删除购物车")
    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo deleteCart(@RequestParam("skuId")Long skuId){
        this.cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }

    @ApiOperation("查询购物车选中状态")
    @GetMapping("user/{userId}")
    @ResponseBody
    public ResponseVo<List<Cart>> queryCheckedCartsByUserId(@PathVariable("userId")Long userId){
        List<Cart> carts = this.cartService.queryCheckedCartsByUserId(userId);
        return ResponseVo.ok(carts);
    }


    @ApiOperation("全选")
    @ResponseBody
    @GetMapping("chooseAll")
    public ResponseVo chooseAll(){
        cartService.chooseAll();
        return ResponseVo.ok();
    }

    @ResponseBody
    @GetMapping("hello")
    public String test() throws Exception {

        return "hello";
    }

}
