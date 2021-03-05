package com.atguigu.gmall.common.aspect;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;


import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-08 18:42
 **/
@Aspect
@Slf4j
@Component
public class GmallAspect {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redisson;
    @Autowired
    RBloomFilter bloomFilter;


    @Around("@annotation(GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        RLock lock1 = redisson.getLock("呵呵");

        log.info("进入到aop了..");
        //获取注解相关参数
        List<Object> args= Arrays.asList(joinPoint.getArgs());

        MethodSignature signature = (MethodSignature)joinPoint.getSignature();

        Method method = signature.getMethod();

        Class returnType = signature.getReturnType();

        GmallCache annotation = method.getAnnotation(GmallCache.class);

        String prefix = annotation.prefix();

        String key =prefix+args;

        String lockPre = annotation.lock();

        int timeout = annotation.timeout();

        //布隆过滤器
        boolean contains = bloomFilter.contains(key);
        if(!contains){
            log.info("布隆过滤器未命中,返回null......");
            return null;
        }
        //加锁,访问缓存
        RLock lock = redisson.getLock(lockPre + args);
        lock.lock();
        try{
            String json = stringRedisTemplate.opsForValue().get(key);
            if(!StringUtils.isBlank(json)){
                return JSON.parseObject(json,returnType);
            }
            //缓存没有,访问数据库
            Object result=joinPoint.proceed(joinPoint.getArgs());
            if(result!=null){
                stringRedisTemplate.opsForValue().set(key,JSON.toJSONString(result),timeout+new Random().nextInt(10), TimeUnit.MINUTES);
            }
            return result;
        }finally{
            //解锁
            lock.unlock();
        }

    }

}
