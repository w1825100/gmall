package com.atguigu.gmall.auth.controller;

import com.atguigu.gmall.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-19 14:41
 **/
@Controller
public class AuthController {

    @Autowired
    AuthService authService;

    @GetMapping("toLogin.html")
    public String toLogin(@RequestParam(value = "returnUrl", required = false) String returUrl, Model model) {
        model.addAttribute("returnUrl", returUrl);
        return "login";
    }

    @PostMapping("login")
    public String login(@RequestParam(value = "returnUrl", required = false, defaultValue = "http://gmall.com") String returnUrl
            , String loginName, String password
            , HttpServletRequest request, HttpServletResponse response) {
        authService.login(loginName, password, request, response);
        if ("http://reg.gmall.com/".equals(returnUrl)) {
            returnUrl = "http://gmall.com";
        }else if("http://order.gmall.com/confirm".equals(returnUrl)){
            returnUrl="http://cart.gmall.com/cart.html";
        }

        return "redirect:" + returnUrl;
    }

}
