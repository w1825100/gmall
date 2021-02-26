package com.atguigu.gmall.cart.service.impl;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.AuthHandler;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.UserInfo;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.expection.GmallException;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @desc 购物车模型 Hash:{redisKey:{skuId:Cart}}
 * @auth lgd
 * @Date 2021/2/22 15:26
 **/
@Slf4j
@Service("cartService")
@SuppressWarnings("all")
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    GmallPmsClient pmsClient;
    @Autowired
    GmallWmsClient wmsClient;
    @Autowired
    GmallSmsClient smsClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    CartMapper cartMapper;
    @Autowired
    CartAsyncService cartAsyncService;


    private static final String CART_PREFIX = "gmall:cart:";
    private static final String CART_PRICE = "gmall:cart:price:";


  /*  @Override
    public void addCart2(Cart cart) {
        String redisKey = getRedisKey();
        String userId = getUserId();
        //外层redisKey
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(redisKey);
        BigDecimal count = cart.getCount();
        Long skuId = cart.getSkuId();

        //判断内层redisKey是否已存在,存在则更新数量,否则新增
        if (hashOps.hasKey(skuId)) {
            cart = (Cart) hashOps.get(skuId);
            log.info("redis中购物车:{}", cart);
            cart.setCount(cart.getCount().add(count));
            //更新redis数据
            hashOps.put(skuId, cart);
            //异步更新mysql
            cartMapper.update(cart, new QueryWrapper<Cart>().eq("sku_id", skuId).eq("user_id", userId));
        } else {
            //新增购物车项 查询sku信息填充cart
            ResponseVo<SkuEntity> skuVo = pmsClient.querySkuById(cart.getSkuId());
            SkuEntity sku = skuVo.getData();
            if (sku == null) {
                throw new GmallException("没有该项商品");
            }
            cart.setUserId(userId);
            cart.setTitle(sku.getTitle());
            cart.setPrice(sku.getPrice());
            cart.setDefaultImage(sku.getDefaultImage());
            ResponseVo<List<SkuAttrValueEntity>> skuAttrValueVo = pmsClient.querySaleAttrValueBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrs = skuAttrValueVo.getData();
            cart.setCheck(true);
            cart.setSaleAttrs(JSON.toJSONString(skuAttrs));
            ResponseVo<List<ItemSaleVo>> listResponseVo = smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> sales = listResponseVo.getData();
            cart.setSales(JSON.toJSONString(sales));
            ResponseVo<List<WareSkuEntity>> wareVo = wmsClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wares = wareVo.getData();
            if (!CollectionUtils.isEmpty(wares)) {
                cart.setStore(wares.stream().anyMatch(ware ->
                                ware.getStock() - ware.getStockLocked() > 0
                        )
                );
            }
            //填充完毕,双写保存
            hashOps.put(skuId, cart);
            cartMapper.insert(cart);
        }
    }
    @Override
    public Cart queryCartBySkuId2(Long skuId) {
        String redisKey = getRedisKey();
        BoundHashOperations hashOps = redisTemplate.boundHashOps(redisKey);
        if (!hashOps.hasKey(skuId)) {
            throw new GmallException("没有该项商品");
        }
        Cart cart = (Cart) hashOps.get(skuId);
        if (cart == null) {
            throw new GmallException("没有该项商品");
        }
        return cart;
    }*/
    //第二种方式,采用stringRedisTemplate


    @Override
    public void addCart(Cart cart) {
        String redisKey = getRedisKey();
        String userId = getUserId();
        //外层redisKey
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(redisKey);
        BigDecimal count = cart.getCount();
        Long skuId = cart.getSkuId();

        //判断内层redisKey是否已存在,存在则更新数量,否则新增
        if (hashOps.hasKey(skuId.toString())) {
            String cartJson = hashOps.get(skuId.toString());
            log.info("redis中已有购物车:{}", cartJson);
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount().add(count));
            //更新redis数据
            hashOps.put(skuId.toString(), JSON.toJSONString(cart));
            //异步更新mysql
            cartAsyncService.update(userId,cart, new QueryWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
        } else {
            log.info("redis中无购物车,执行新增..");
            //新增购物车项 查询sku信息填充cart
            ResponseVo<SkuEntity> skuVo = pmsClient.querySkuById(cart.getSkuId());
            SkuEntity sku = skuVo.getData();
            if (sku == null) {
                throw new GmallException("没有该项商品");
            }
            cart.setUserId(userId);
            cart.setTitle(sku.getTitle());
            cart.setPrice(sku.getPrice());
            cart.setDefaultImage(sku.getDefaultImage());
            ResponseVo<List<SkuAttrValueEntity>> skuAttrValueVo = pmsClient.querySaleAttrValueBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrs = skuAttrValueVo.getData();
            cart.setCheck(true);
            cart.setSaleAttrs(JSON.toJSONString(skuAttrs));
            ResponseVo<List<ItemSaleVo>> listResponseVo = smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> sales = listResponseVo.getData();
            cart.setSales(JSON.toJSONString(sales));
            ResponseVo<List<WareSkuEntity>> wareVo = wmsClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wares = wareVo.getData();
            if (!CollectionUtils.isEmpty(wares)) {
                cart.setStore(wares.stream().anyMatch(ware ->
                                ware.getStock() - ware.getStockLocked() > 0
                        )
                );
            }
            //填充完毕,双写保存
            hashOps.put(skuId.toString(), JSON.toJSONString(cart));
            cartAsyncService.insert(userId,cart);
            //添加价格缓存
            stringRedisTemplate.opsForValue().set(CART_PRICE + skuId, sku.getPrice().toString());

        }
    }

    @Override
    public List<Cart> queryCarts() {
        //1.根据userKey查询购物车
        String userKey = AuthHandler.getUserInfo().getUserKey();
        String temKey = CART_PREFIX + userKey;

        BoundHashOperations<String, String, String> unLoginhashOps = stringRedisTemplate.boundHashOps(temKey);
        List<String> cartUnLoginJsons = unLoginhashOps.values();
        List<Cart> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(cartUnLoginJsons)) {
            unLoginCarts = cartUnLoginJsons.stream().map(
                    cartJson -> {
                        Cart cart = JSON.parseObject(cartJson, Cart.class);
                        //查询时获取最新价格
                        cart.setCurrentPrice(new BigDecimal(stringRedisTemplate.opsForValue().get(CART_PRICE + cart.getSkuId())));
                        return cart;
                    }
            ).collect(Collectors.toList());
        }
        Long userId = AuthHandler.getUserInfo().getUserId();
        //2.未登录直接返回
        if (userId == null) {
            return unLoginCarts;
        }
        // 3.登录状态合并购物车
        String loginKey = CART_PREFIX + userId;
        BoundHashOperations<String, String, String> loginHashOps = stringRedisTemplate.boundHashOps(loginKey);
        List<String> values = loginHashOps.values();
        if (!CollectionUtils.isEmpty(unLoginCarts)){
            unLoginCarts.forEach(unLogincart -> {
                String skuId = unLogincart.getSkuId().toString();
                if (loginHashOps.hasKey(skuId)) {
                    //登录状态已有该商品
                    Cart loginCart = JSON.parseObject(loginHashOps.get(skuId), Cart.class);
                    loginCart.setCount(loginCart.getCount().add(unLogincart.getCount()));
                    loginHashOps.put(skuId, JSON.toJSONString(loginCart));
                    //异步更新mysql
                    this.cartAsyncService.update(userId.toString(),loginCart, new QueryWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
                }
                else {
                    //没有该商品,新增一条记录
                    unLogincart.setUserId(userId.toString());
                    loginHashOps.put(skuId, JSON.toJSONString(unLogincart));
                    //异步写入mysql
                    cartAsyncService.insert(userId.toString(),unLogincart);
                }
                //4.删除未登录购物车
                stringRedisTemplate.delete(temKey);
                cartAsyncService.deleteByUserId(userKey);
            });
        }
        //5.汇总,返回
        List<String> loginCarts = loginHashOps.values();
        if (CollectionUtils.isEmpty(loginCarts)) {
            return null;
        }
        return loginCarts.stream().map(cartJson -> {
            Cart cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCurrentPrice(new BigDecimal(stringRedisTemplate.opsForValue().get(CART_PRICE + cart.getSkuId())));
            return cart;
        }).collect(Collectors.toList());
    }

    @Override
    public Cart queryCartBySkuId(Long skuId) {
        String redisKey = getRedisKey();
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(redisKey);
        if (!hashOps.hasKey(skuId.toString())) {
            throw new GmallException("没有该项商品");
        }
        String cartJSon = hashOps.get(skuId.toString());
        if (StringUtils.isBlank(cartJSon)) {
            throw new GmallException("没有该项商品");
        }
        return JSON.parseObject(cartJSon, Cart.class);
    }


    //根据用户状态获得redisKey
    private String getRedisKey() {
        String redisKey;
        UserInfo userInfo = AuthHandler.getUserInfo();
        Long userId = userInfo.getUserId();
        if (userId != null) {
            redisKey = CART_PREFIX + userId;
        } else {
            redisKey = CART_PREFIX + userInfo.getUserKey();
        }
        return redisKey;
    }

    //根据用户状态获得数据库user_id
    private String getUserId() {
        Long userId = AuthHandler.getUserInfo().getUserId();
        if (userId == null) {
            return AuthHandler.getUserInfo().getUserKey();
        } else {
            return userId.toString();
        }
    }


    public void updateNum(Cart cart) {
        String userId = this.getUserId();
        String key = this.getRedisKey();

        BoundHashOperations<String, String, String> hashOps = this.stringRedisTemplate.boundHashOps(key);
        if (!hashOps.hasKey(cart.getSkuId().toString())) {
            throw new GmallException("该用户没有对应的购物车记录");
        }

        // 用户要更新的数量
        BigDecimal count = cart.getCount();

        // 查询redis中的购物车记录
        String json = hashOps.get(cart.getSkuId().toString()).toString();
        cart = JSON.parseObject(json, Cart.class);
        cart.setCount(count); // 更新购物车中的商品数量

        hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        this.cartAsyncService.update(userId,cart, new QueryWrapper<Cart>().eq("user_id", userId).eq("sku_id", cart.getSkuId()));
    }

    public void deleteCart(Long skuId) {
        String key = this.getRedisKey();
        String userId = this.getUserId();
        BoundHashOperations<String, String, String> hashOps = this.stringRedisTemplate.boundHashOps(key);
        hashOps.delete(skuId.toString());
        this.cartAsyncService.deleteCart(userId, skuId);
    }

    public List<Cart> queryCheckedCartsByUserId(Long userId) {
        String key = CART_PREFIX + userId;

        BoundHashOperations<String, String, String> hashOps = this.stringRedisTemplate.boundHashOps(key);
        List<String> cartJsons = hashOps.values();
        if (CollectionUtils.isEmpty(cartJsons)) {
            throw new GmallException("该用户没有购物车记录");
        }

        return cartJsons.stream().map(cartJson -> JSON.parseObject(cartJson.toString(), Cart.class)).filter(Cart::getCheck).collect(Collectors.toList());
    }

    @Override
    public void updateStatus(Cart cart) {
        String redisKey = getRedisKey();
        String userId = getUserId();
        String skuId = cart.getSkuId().toString();
        Boolean check = cart.getCheck();
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(redisKey);
        if (!hashOps.hasKey(cart.getSkuId().toString())) {
            throw new GmallException("更新状态失败,购物车无此商品");
        }
        String cartJson = hashOps.get(skuId);
        cart = JSON.parseObject(cartJson, Cart.class);
        cart.setCheck(check);
        hashOps.put(skuId, JSON.toJSONString(cart));
        cartAsyncService.update(userId,cart, new QueryWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
    }

    @Override
    public void chooseAll() {
        String redisKey = getRedisKey();
        String userId = getUserId();
        BoundHashOperations<String, String, String> hashOperations = stringRedisTemplate.boundHashOps(redisKey);

        List<String> cartsJson = hashOperations.values();
        if (CollectionUtils.isEmpty(cartsJson)) {
            return;
        }
        List<Cart> carts = cartsJson.stream().map(cartJson -> JSON.parseObject(cartJson, Cart.class)).collect(Collectors.toList());
        carts.forEach(cart -> {
                    cart.setCheck(true);
                    hashOperations.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
                }
        );
        this.cartAsyncService.chooseAll(userId);
    }

}
