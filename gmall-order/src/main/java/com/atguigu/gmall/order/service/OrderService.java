package com.atguigu.gmall.order.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.UserInfo;
import com.atguigu.gmall.common.expection.GmallException;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.order.interceptor.OrderInterceptor;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-26 08:49
 **/
@Service
@Slf4j
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
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    GmallOmsClient omsClient;


    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        //1拦截器获取用户id
        Long userId = OrderInterceptor.getUserInfo().getUserId();
        //2用户地址列表
        ResponseVo<List<UserAddressEntity>> addressVO = umsClient.queryUserAddressByUserId(userId);
        List<UserAddressEntity> addresses = addressVO.getData();
        if (!CollectionUtils.isEmpty(addresses)) {
            confirmVo.setAddresses(addresses);
        }
        //3用户选中购物车集合
        ResponseVo<List<Cart>> cartResponse = cartClient.queryCheckedCartsByUserId(userId);
        List<Cart> carts = cartResponse.getData();
        if (CollectionUtils.isEmpty(carts)) {
            throw new GmallException("没有选中任何商品!");
        }
        //遍历购物车选中商品的集合,获得id,数量
        List<OrderItemVo> items = carts.stream().map(cart -> {
            OrderItemVo itemVo = new OrderItemVo();
            //1.获取购物车中skuId和count
            Long skuId = cart.getSkuId();
            BigDecimal count = cart.getCount();
            itemVo.setSkuId(skuId);
            itemVo.setCount(count);
            //2.查询sku最新信息
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(skuId);
            SkuEntity sku = skuEntityResponseVo.getData();
            if (sku != null) {
                BeanUtils.copyProperties(sku, itemVo);
            }
            //3.营销信息
            ResponseVo<List<ItemSaleVo>> SaleVo = smsClient.querySalesBySkuId(skuId);
            List<ItemSaleVo> sales = SaleVo.getData();
            if (!CollectionUtils.isEmpty(sales)) {
                itemVo.setSales(sales);
            }
            //4.销售属性
            ResponseVo<List<SkuAttrValueEntity>> skuAttrVo = pmsClient.querySaleAttrValueBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrs = skuAttrVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrs)) {
                itemVo.setSaleAttrs(skuAttrs);
            }
            //5.库存信息
            ResponseVo<List<WareSkuEntity>> wareVO = wmsClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wares = wareVO.getData();
            if (!CollectionUtils.isEmpty(wares)) {
                itemVo.setStore(wares.stream().anyMatch(
                        ware -> ware.getStock() - ware.getStockLocked() > 0
                ));
            }

            return itemVo;

        }).collect(Collectors.toList());
        confirmVo.setOrderItems(items);
        //获得用户自身积分信息
        ResponseVo<UserEntity> userVo = umsClient.queryUserById(userId);
        UserEntity user = userVo.getData();
        if (user == null) {
            throw new GmallException("该用户不存在");
        }
        confirmVo.setBounds(user.getIntegration());
        // 防重（提交订单的幂等性）
        String orderToken = IdWorker.getTimeId();

        confirmVo.setOrderToken(orderToken);
        this.redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, orderToken);

        return confirmVo;
    }


    public OrderEntity submit(OrderSubmitVo submitVo) {

        // 1.防重（幂等性）
        String orderToken = submitVo.getOrderToken();
        if (StringUtils.isBlank(orderToken)) {
            log.warn("token为空,返回");
            throw new GmallException("非法请求！");
        }
        String script = "if(redis.call('get',KEYS[1]) == ARGV[1]) then return redis.call('del',KEYS[1]) else return 0 end";
        Boolean flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(KEY_PREFIX + orderToken), orderToken);
        if (!flag) {
            log.warn("token重复提交,返回");
            throw new GmallException("请勿重复提交！");
        }
        //2.验证价格
        List<com.atguigu.gmall.oms.vo.OrderItemVo> items = submitVo.getItems();// 获取订单确认页提交的送货清单
        if (CollectionUtils.isEmpty(items)) {
            log.warn("购物车为空,返回");
            throw new GmallException("请选择要购买的商品");
        }
        BigDecimal totalPrice = submitVo.getTotalPrice(); // 页面传递的总价格
        // 获取数据库中的实时总价格
        BigDecimal currentTotalFee = items.stream().map(item -> {
            ResponseVo<SkuEntity> skusVo = pmsClient.querySkuById(item.getSkuId());
            SkuEntity sku = skusVo.getData();
            if (sku == null) {
                return new BigDecimal(0);
            }
            return sku.getPrice().multiply(item.getCount());
        }).reduce((a, b) -> a.add(b)).get();
        if(totalPrice.compareTo(currentTotalFee)!=0){
            log.warn("价格校验不通过,返回");
            throw new GmallException("页面已过期,请稍后重试");
        }
        //锁库存
        // 3.验库存并锁定库存
        List<SkuLockVo> lockVos = items.stream().map(item -> { // 把送货清单集合转化为skuLockVo集合
            SkuLockVo skuLockVo = new SkuLockVo();
            skuLockVo.setSkuId(item.getSkuId());
            skuLockVo.setCount(item.getCount().intValue());
            return skuLockVo;
        }).collect(Collectors.toList());
        ResponseVo<List<SkuLockVo>> skuLockResponseVo = this.wmsClient.checkAndLock(lockVos, orderToken);
        List<SkuLockVo> skuLockVos = skuLockResponseVo.getData();
        // 如果验库存锁库存的返回值不为空，说明验库存和锁库存失败，提示锁定信息
        if (!CollectionUtils.isEmpty(skuLockVos)){
            log.warn("wms锁定库存失败,返回:{}",skuLockVos);
            throw new GmallException(JSON.toJSONString(skuLockVos));
        }

        // 4.创建订单
        OrderEntity orderEntity = null;
        UserInfo userInfo =OrderInterceptor.getUserInfo();
        try {
            ResponseVo<OrderEntity> orderEntityResponseVo = this.omsClient.saveOrder(submitVo, userInfo.getUserId());
            orderEntity = orderEntityResponseVo.getData();
            log.warn("order微服务创建了新订单,给oms延时队列发送订单信息:{},90秒后自动关单",orderEntity);
            this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "order.create", orderToken);
        } catch (Exception e) {
            log.warn("order微服务出现异常,订单进入关闭状态,立即发送给oms微服务死信队列关闭订单:{}",orderToken);
            e.printStackTrace();
            // 不管什么异常，直接发送消息给OMS更新订单状态，订单存在更新为无效订单，订单不存在影响条数为0
            this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "order.dead", orderToken);
            throw new GmallException("创建订单失败，请联系运行人员。");
        }

        // 5.删除购物车中的对应记录
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userInfo.getUserId());
        // 获取送货清单中所有商品的skuId
        List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        map.put("skuIds", JSON.toJSONString(skuIds));
        log.warn("order微服务订单创建完毕,给cart微服务发送删除购物车消息,map:{}",map);
        this.rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "cart.delete", map);

        return orderEntity;
    }
}
