package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.aspect.GmallCache;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

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



    /**
    *   @desc  查询一级分类
    *   @auth lgd
    *   @Date 2021/2/6 21:29
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
    *   @desc  查询二级分类及二级分类下三级分类
    *   @auth 刘广典
    *   @Date 2021/2/6 21:34
    **/

    @ResponseBody
    @GetMapping("index/cates/{pid}")
    public ResponseVo<List<CategoryEntity>> getLv2SubCategories(@PathVariable Long pid) {
       List<CategoryEntity> res = indexService.getSubCategories(pid);
       return ResponseVo.ok(res);
    }

    /**
    *   @desc  测试分布式锁
    *   @auth 刘广典
    *   @Date 2021/2/6 21:34
    **/
    @ResponseBody
    @GetMapping("index/test/lock")
    public ResponseVo test() {
        //唯一标识,防止锁误删
        String uuid= UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid,3,TimeUnit.SECONDS);
        if (!lock){
            try {
                //自旋 等待锁
                Thread.sleep(50);
                test();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            String number = stringRedisTemplate.opsForValue().get("number");
            int i = Integer.parseInt(number);
            stringRedisTemplate.opsForValue().set("number", String.valueOf(++i));
            //此处必须保证判断和删除的原子性
            String script ="if(redis.call('get',KEYS[1]) == ARGV[1]) then return redis.call('del',KEYS[1]) else return 0 end";
            stringRedisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList("lock"),uuid);
            return ResponseVo.ok();
        }
       return null;
    }
}
