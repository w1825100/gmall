package com.atguigu.gmall.msg.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.msg.service.MsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: gmall
 * @description: 短信发送
 * @author: lgd
 * @create: 2021-02-17 01:11
 **/
@RestController
@RequestMapping("msg")
public class MsgController {
    @Autowired
    MsgService msgService;

    @GetMapping("send/{phone}")
    public ResponseVo<Boolean> send(@PathVariable String phone){
       Boolean b= msgService.send(phone);
        return ResponseVo.ok(b);
    }
}

