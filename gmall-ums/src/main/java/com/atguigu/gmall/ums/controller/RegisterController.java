package com.atguigu.gmall.ums.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-17 03:11
 **/
@Controller
public class RegisterController {

    @GetMapping()
    public String toReg(){
        return "register";
    }
    @GetMapping("login.html")
    public String toLogin(){
        return "login";
    }
    @GetMapping("register.html")
    public String toReg1(){
        return "register";
    }
}
