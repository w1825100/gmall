package com.atguigu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;

import com.atguigu.gmall.oms.entity.OrderEntity;

import com.atguigu.gmall.payment.pojo.PayAsyncVo;

import com.atguigu.gmall.payment.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@Controller
public class PaymentController {


    @Autowired
    private PaymentService paymentService;


    //生成订单,
    @GetMapping("pay.html")
    public String toPay(@RequestParam("orderToken") String orderToken, Model model) {
        OrderEntity orderEntity = paymentService.toPay(orderToken);
        model.addAttribute("orderEntity", orderEntity);
        return "pay";
    }

    @GetMapping("alipay.html")
    @ResponseBody //生成支付宝页面
    public String alipay(@RequestParam("orderToken") String orderToken) throws AlipayApiException {
        String form = paymentService.aliPay(orderToken);
        return form;
    }

    //同步回调
    @GetMapping("pay/success")
    public String paysuccess(HttpServletRequest request, Model model) {
        String total_amount = request.getParameter("total_amount");
        model.addAttribute("total_amount", total_amount);
        return "paysuccess";
    }

    @PostMapping("pay/ok")
    @ResponseBody
    public Object payOk(PayAsyncVo payAsyncVo) {
        String result = paymentService.payOk(payAsyncVo);
        return result;
    }
}
