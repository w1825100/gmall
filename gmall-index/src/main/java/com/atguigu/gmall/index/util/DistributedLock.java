package com.atguigu.gmall.index.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;


import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @program: gmall
 * @description: 重入锁工具类
 * @author: lgd
 * @create: 2021-03-18 09:58
 **/
@Component
@Slf4j
public class DistributedLock {


    @Autowired
    StringRedisTemplate redisTemplate;
    private Timer timer;

    public Boolean tryLock(String key, String uuid, Integer expire) {
        //如果锁不存在,直接获取,如果锁存在,判断是否自己的锁,设置过期时间
        String script = "if(redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1], ARGV[1]) == 1) then " +
                "   redis.call('hincrby', KEYS[1], ARGV[1], 1) " +
                "   redis.call('expire', KEYS[1], ARGV[2]) " +
                "   return 1 " +
                "else " +
                "   return 0 " +
                "end";
        Boolean flag = redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(key), uuid, expire.toString());
        if (!flag) {
            try {
                //加锁失败,进行自旋
                Thread.sleep(50);
                tryLock(key, uuid, expire);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }log.error("加锁成功,lock:{},uid{}",key,uuid);
        //加锁成功
        flushTime(key,uuid,expire);
        return true;
    }

    public void unlock(String key, String uuid) {
        String script ="if(redis.call('hexists', KEYS[1], ARGV[1]) == 0) then " +
                "   return nil " +
                "elseif(redis.call('hincrby', KEYS[1], ARGV[1], -1) == 0) then " +
                "   return redis.call('del', KEYS[1]) " +
                "else return 0 " +
                "end";
        Long ex = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(key), uuid);
        if (ex == null) {
            log.error("你在玩火!尝试解别人的锁");
        }else if (ex==1){
            log.error("解锁成功,准备清除定时器:lock,uuid,{},{}",key,uuid);
            timer.cancel();
            log.error("定时器已解除");
        }else if(ex==0){
            log.error("出来了一次");
        }
    }

    public void flushTime(String lockName, String uuid, Integer expire){
        String script = "if(redis.call('hexists', KEYS[1], ARGV[1]) == 1) then " +
                "   return redis.call('expire', KEYS[1], ARGV[2]) " +
                "else " +
                "   return 0 " +
                "end";
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expire.toString());
            }
        }, expire * 1000 / 3, expire * 1000 / 3);
    }

}
