package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.config.GmallCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.Redisson;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UnknownFormatConversionException;
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
public class GmallIndexAspect {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedissonClient redisson;
    @Autowired
    RBloomFilter bloomFilter;


    @Pointcut("@annotation(com.atguigu.gmall.index.config.GmallCache)")
    public void hehe(){

    }



    @Around("hehe()")
    public Object aound(ProceedingJoinPoint joinPoint) throws Throwable {
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
        if(!contains) return null;

        String json = stringRedisTemplate.opsForValue().get(key);

        if(!StringUtils.isBlank(json)){
           return JSON.parseObject(json,returnType);
        }
        RLock lock = redisson.getLock(lockPre + args);
        lock.lock();
        String json1 = stringRedisTemplate.opsForValue().get(key);

        if(!StringUtils.isBlank(json1)){
            return JSON.parseObject(json1,returnType);
        }
        Object result=joinPoint.proceed(joinPoint.getArgs());
        if(result!=null){
        stringRedisTemplate.opsForValue().set(key,JSON.toJSONString(result),new Random().nextInt(timeout), TimeUnit.MINUTES);
        }else{
            stringRedisTemplate.opsForValue().set(key,JSON.toJSONString(result),5, TimeUnit.MINUTES);
        }
        return result;
    }

}
