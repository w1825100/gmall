package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.aspect.GmallCache;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-05 02:15
 **/
@Service
public class IndexService {
    @Autowired
    GmallPmsClient gmallPmsClient;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redissonClient;

    private static final String KEY_PREFIX = "gmall:index:cates:";

    public List<CategoryEntity> queryLV1Categories() {
        ResponseVo<List<CategoryEntity>> categoryEntityResponseVo = gmallPmsClient.queryCategoryListByPid(0l);
        return categoryEntityResponseVo.getData();
    }
    //使用aop注解缓存
    @GmallCache(prefix = KEY_PREFIX,timeout = 300,random = 300,lock="gmall:index:cates:lock:")
    public List<CategoryEntity> getSubCategories(Long pid) {
        return  gmallPmsClient.getSubsCategories(pid).getData();
    }

    public ResponseVo<List<CategoryEntity>> getSubCategories2(Long pid) {
        //先查询缓存.缓存中有,直接返回
        String json = stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(json)) {
            return ResponseVo.ok(JSON.parseArray(json, CategoryEntity.class));
        }
        // 为了防止缓存击穿，添加分布式锁
        RLock lock = this.redissonClient.getLock("index:cates:lock:" + pid);
        lock.lock();
        String json2 = stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(json)) {
            return ResponseVo.ok(JSON.parseArray(json2, CategoryEntity.class));
        }

        ResponseVo<List<CategoryEntity>> categoryEntityResponseVo = gmallPmsClient.getSubsCategories(pid);
        List<CategoryEntity> data = categoryEntityResponseVo.getData();

        // 放入缓存
        if (CollectionUtils.isEmpty(data)){
            //为了防止缓存穿透,缓存空对象,设置较短的失效时间
            this.stringRedisTemplate.opsForValue().set(KEY_PREFIX + pid, null, 3, TimeUnit.MINUTES);
        } else {
            // 为了防止缓存雪崩，给缓存时间添加随机值
            this.stringRedisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(data), 30 + new Random().nextInt(10), TimeUnit.DAYS);
        }
        lock.unlock();
        return categoryEntityResponseVo;
    }

}
