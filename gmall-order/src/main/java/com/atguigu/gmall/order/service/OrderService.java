package com.atguigu.gmall.order.service;

import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.expection.GmallException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.order.config.OrderInterceptor;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.order.vo.OrderItemVo;
import com.atguigu.gmall.order.vo.OrderSubmitVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-26 08:49
 **/
@Service
public class OrderService {
    @Autowired
    GmallPmsClient pmsClient;
    @Autowired
    GmallSmsClient smsClient;
    @Autowired
    GmallWmsClient wmsClient;
    @Autowired
    GmallUmsClient umsClient;
    @Autowired
    GmallCartClient cartClient;
    private static final String KEY_PREFIX = "gmall:order:token:";
    @Autowired
    StringRedisTemplate redisTemplate;

    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        //1拦截器获取用户id
        Long userId= OrderInterceptor.getUserInfo().getUserId();
        //2用户地址列表
        ResponseVo<List<UserAddressEntity>> addressVO = umsClient.queryUserAddressByUserId(userId);
        List<UserAddressEntity> addresses = addressVO.getData();
        if(!CollectionUtils.isEmpty(addresses)){
            confirmVo.setAddresses(addresses);
        }
        //3用户选中购物车集合
        ResponseVo<List<Cart>> cartResponse = cartClient.queryCheckedCartsByUserId(userId);
        List<Cart> carts = cartResponse.getData();
        if(CollectionUtils.isEmpty(carts)){
            throw new GmallException("没有选中任何商品!");
        }
        //遍历购物车选中商品的集合,获得id,数量
        List<OrderItemVo> items = carts.stream().map(cart -> {
            OrderItemVo itemVo=new OrderItemVo();
            //1.获取购物车中skuId和count
            Long skuId = cart.getSkuId();
            BigDecimal count = cart.getCount();
            itemVo.setSkuId(skuId);
            itemVo.setCount(count);
            //2.查询sku最新信息
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(skuId);
            SkuEntity sku = skuEntityResponseVo.getData();
            if(sku!=null){
                BeanUtils.copyProperties(sku,itemVo);
            }
            //3.营销信息
            ResponseVo<List<ItemSaleVo>> SaleVo = smsClient.querySalesBySkuId(skuId);
            List<ItemSaleVo> sales = SaleVo.getData();
            if(!CollectionUtils.isEmpty(sales)){
                itemVo.setSales(sales);
            }
            //4.销售属性
            ResponseVo<List<SkuAttrValueEntity>> skuAttrVo = pmsClient.querySaleAttrValueBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrs = skuAttrVo.getData();
            if(!CollectionUtils.isEmpty(skuAttrs)){
                itemVo.setSaleAttrs(skuAttrs);
            }
            //5.库存信息
            ResponseVo<List<WareSkuEntity>> wareVO = wmsClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wares = wareVO.getData();
            if(!CollectionUtils.isEmpty(wares)){
               itemVo.setStore(wares.stream().anyMatch(
                       ware->ware.getStock()-ware.getStockLocked()>0
               ));
            }

            return itemVo;

        }).collect(Collectors.toList());
        confirmVo.setOrderItems(items);
        //获得用户自身积分信息
        ResponseVo<UserEntity> userVo = umsClient.queryUserById(userId);
        UserEntity user = userVo.getData();
        if(user==null){
          throw new GmallException("该用户不存在");
        }
        confirmVo.setBounds(user.getIntegration());
        //token
        // 防重（提交订单的幂等性）
        String orderToken = IdWorker.getTimeId();

        confirmVo.setOrderToken(orderToken);
        this.redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, orderToken);

        return confirmVo;
    }


    public OrderEntity submit(OrderSubmitVo submitVo) {
            //TODO






        return null;
    }
}
