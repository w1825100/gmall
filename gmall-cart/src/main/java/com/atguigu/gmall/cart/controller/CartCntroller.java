package com.atguigu.gmall.cart.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-21 13:45
 **/
@RestController
public class CartCntroller {


    @GetMapping("hello")
    public String test(){
        return "hello";
    }
}
