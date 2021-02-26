package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.order.service.OrderService;

import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.order.vo.OrderSubmitVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-26 08:49
 **/
@Controller
public class OrderController {
    @Autowired
    OrderService orderService;


    @ApiOperation("订单确认页")
    @GetMapping("confirm")
    public ModelAndView confirm(ModelAndView modelAndView){
        OrderConfirmVo confirm =orderService.confirmOrder();
        modelAndView.addObject("confirmVo",confirm);
        modelAndView.setViewName("trade");
        return modelAndView;
    }

    @ApiOperation("下单")
    @PostMapping("submit")
    public ResponseVo<String>submit(OrderSubmitVo submitVo){
        OrderEntity orderEntity = this.orderService.submit(submitVo);
        return ResponseVo.ok(orderEntity.getOrderSn());
    }

}
