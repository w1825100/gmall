package com.atguigu.gmall.payment.service;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.expection.GmallException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.config.AlipayTemplate;
import com.atguigu.gmall.payment.feign.GmallOmsClient;
import com.atguigu.gmall.payment.interceptor.LoginInterceptor;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.pojo.PayAsyncVo;
import com.atguigu.gmall.payment.pojo.PayVo;
import com.atguigu.gmall.payment.pojo.PaymentInfoEntity;
import com.atguigu.gmall.payment.pojo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;


@Service
@Slf4j
@SuppressWarnings("all")
public class PaymentService {

    @Autowired
    private GmallOmsClient omsClient;
    @Autowired
    private PaymentInfoMapper paymentInfoMapper;
    @Autowired
    private AlipayTemplate alipayTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;


    public OrderEntity queryOrderByToken(String orderToken) {
        ResponseVo<OrderEntity> orderEntityResponseVo = this.omsClient.queryOrderByToken(orderToken);
        return orderEntityResponseVo.getData();
    }

    public String savePayment(OrderEntity orderEntity) {
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setPaymentStatus(0);
        paymentInfoEntity.setPaymentType(orderEntity.getPayType());
        paymentInfoEntity.setTotalAmount(orderEntity.getTotalAmount());
        paymentInfoEntity.setSubject("谷粒商城订单支付平台");
        paymentInfoEntity.setOutTradeNo(orderEntity.getOrderSn());
        paymentInfoEntity.setCreateTime(new Date());
        this.paymentInfoMapper.insert(paymentInfoEntity);
        return paymentInfoEntity.getId().toString();
    }

    public PaymentInfoEntity queryPaymentById(String payId){
        return this.paymentInfoMapper.selectById(payId);
    }

    public int updatePaymentInfo(PayAsyncVo payAsyncVo, String payId) {
        PaymentInfoEntity paymentInfoEntity = this.paymentInfoMapper.selectById(payId);
        paymentInfoEntity.setTradeNo(payAsyncVo.getTrade_no());
        paymentInfoEntity.setPaymentStatus(1);
        paymentInfoEntity.setCallbackTime(new Date());
        paymentInfoEntity.setCallbackContent(JSON.toJSONString(payAsyncVo));
        return this.paymentInfoMapper.updateById(paymentInfoEntity);
    }

    public OrderEntity toPay(String orderToken) {

        OrderEntity orderEntity = this.queryOrderByToken(orderToken);
        if (orderEntity == null) {
            throw new GmallException("要支付的订单不存在。");
        }
        // 判断订单是否属于该用户
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() != orderEntity.getUserId()){
            throw new GmallException("该订单不属于您，或者您没有支付权限");
        }
        // 判断订单是否属于待付款状态
        if (orderEntity.getStatus() != 0){
            throw new GmallException("该订单无法支付，请注意您的订单状态");
        }
        return orderEntity;
    }


    public String aliPay(String orderToken) {

        OrderEntity orderEntity = this.queryOrderByToken(orderToken);
        if (orderEntity == null) {
            throw new GmallException("要支付的订单不存在。");
        }
        // 判断订单是否属于该用户
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() != orderEntity.getUserId()){
            throw new GmallException("该订单不属于您，或者您没有支付权限");
        }
        // 判断订单是否属于待付款状态
        if (orderEntity.getStatus() != 0){
            throw new GmallException("该订单无法支付，请注意您的订单状态");
        }

        // 调用阿里的支付接口，跳转到支付页面
        PayVo payVo = new PayVo();
        payVo.setOut_trade_no(orderToken);
        // 注意：一定不要使用实际价格，建议直接使用0.01
        payVo.setTotal_amount(orderEntity.getTotalAmount().setScale(2).toString());
        payVo.setSubject("谷粒商城订单支付平台");
        // 生成对账记录
        String payId = this.savePayment(orderEntity);
        payVo.setPassback_params(payId);
        try {
            String form = this.alipayTemplate.pay(payVo);
            return form;
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new GmallException("生成支付宝页面失败");

        }


    }
    //异步回调
    public String payOk(PayAsyncVo payAsyncVo) {
        log.warn("支付宝异步回调了..");
        // 1.验签
        Boolean flag = this.alipayTemplate.checkSignature(payAsyncVo);
        if (!flag){
            log.warn("验签不通过,返回");
            return "failure";
        }

        // 2.校验业务参数：app_id、out_trade_no、total_amount
        String app_id = payAsyncVo.getApp_id();
        String out_trade_no = payAsyncVo.getOut_trade_no();
        String total_amount = payAsyncVo.getTotal_amount();
        String payId = payAsyncVo.getPassback_params(); // 对账记录的id
        PaymentInfoEntity paymentInfoEntity = this.queryPaymentById(payId);
        if (!StringUtils.equals(app_id, this.alipayTemplate.getApp_id())
                || new BigDecimal(total_amount).compareTo(paymentInfoEntity.getTotalAmount())  != 0
                || !StringUtils.equals(out_trade_no, paymentInfoEntity.getOutTradeNo())
        ) {
            log.warn("参数不对应,返回");
            return "failure";
        }

        // 3.校验支付状态
        String trade_status = payAsyncVo.getTrade_status();
        if (!StringUtils.equals("TRADE_SUCCESS", trade_status)){
            log.warn("支付状态不正常");
            return "failure";
        }

        // 4.更新支付对账表中状态
        if (this.updatePaymentInfo(payAsyncVo, payId) == 0) {
            log.warn("更新对账表失败");
            return "failure";
        }
        log.warn("验签通过,payment微服务给oms发消息,更新订单状态,减少库存");
        String passback_params = payAsyncVo.getPassback_params();
        // 5.发送消息给订单（oms 发送消息给wms 减库存）
        this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "order.success", out_trade_no);

        // 6.响应信息给支付宝
        return "success";

    }
}
