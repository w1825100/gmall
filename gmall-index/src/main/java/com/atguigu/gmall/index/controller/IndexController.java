package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.aspect.GmallCache;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.index.util.DistributedLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @program: gmall
 * @description: 首页
 * @author: lgd
 * @create: 2021-02-05 02:12
 **/
@Controller
public class IndexController {
    @Autowired
    IndexService indexService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    DistributedLock distributedLock;
    @Autowired
    RedissonClient redissonClient;


    /**
     * @desc 查询一级分类
     * @auth lgd
     * @Date 2021/2/6 21:29
     **/
    @GetMapping
    public String toIndex(Model model) {
        List<CategoryEntity> categories = indexService.queryLV1Categories();
        model.addAttribute("categories", categories);
        return "index";
    }

    @GetMapping("index.html")
    public String toIndex1(Model model) {
        List<CategoryEntity> categories = indexService.queryLV1Categories();
        model.addAttribute("categories", categories);
        return "index";
    }

    /**
     * @desc 查询二级分类及二级分类下三级分类
     * @auth 刘广典
     * @Date 2021/2/6 21:34
     **/

    @ResponseBody
    @GetMapping("index/cates/{pid}")
    public ResponseVo<List<CategoryEntity>> getLv2SubCategories(@PathVariable Long pid) {
        List<CategoryEntity> res = indexService.getSubCategories(pid);
        return ResponseVo.ok(res);
    }

    /**
     * @desc 测试分布式锁
     * @auth 刘广典
     * @Date 2021/2/6 21:34
     **/
    @ResponseBody
    @GetMapping("index/test/lock")
    public ResponseVo test() {
        //唯一标识,防止锁误删 TODO//自动续期
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);
        if (!lock) {
            try {
                //自旋 等待锁
                Thread.sleep(50);
                test();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            String number = stringRedisTemplate.opsForValue().get("number");
            int i = Integer.parseInt(number);
            stringRedisTemplate.opsForValue().set("number", String.valueOf(++i));
            //此处必须保证判断和删除的原子性
            String script = "if(redis.call('get',KEYS[1]) == ARGV[1]) then return redis.call('del',KEYS[1]) else return 0 end";
            stringRedisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList("lock"), uuid);
            return ResponseVo.ok();
        }
        return null;
    }

    /**
     * @desc 测试分布式锁2
     * @auth 刘广典
     * @Date 2021/2/6 21:34
     **/
    @ResponseBody
    @GetMapping("index/test/lock2")
    public ResponseVo test1() {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = distributedLock.tryLock("lock", uuid, 30);
        if (lock) {
            String number = stringRedisTemplate.opsForValue().get("number");
            int num = Integer.parseInt(number);
            stringRedisTemplate.opsForValue().set("number", String.valueOf(++num));
            distributedLock.unlock("lock", uuid);
        }
        return ResponseVo.ok();
    }

    /**
    *   @desc  模拟秒杀业务
    *   @auth lgd
    *   @Date 2021/3/18 16:18
    **/
    @ResponseBody
    @GetMapping("index/seckill/{skuId}")
    public ResponseVo seckill(@PathVariable String skuId){
        RLock lock=null;
        try {
            lock = redissonClient.getFairLock("seckill:lock:"+skuId);
            lock.lock();
            String stockStr = this.stringRedisTemplate.opsForValue().get("seckill:stock:" + skuId);
            if (StringUtils.isBlank(stockStr)||Integer.parseInt(stockStr)==0){
                return ResponseVo.fail("手慢了!秒杀已完毕");
            }
            this.stringRedisTemplate.opsForValue().decrement("seckill:stock:"+skuId);
            String orderSn = UUID.randomUUID().toString();
            return  ResponseVo.ok("恭喜你,秒杀成功"+orderSn);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }


}
