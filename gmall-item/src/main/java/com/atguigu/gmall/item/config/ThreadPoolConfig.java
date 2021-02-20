package com.atguigu.gmall.item.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-16 02:08
 **/
@Configuration
@Slf4j
public class ThreadPoolConfig {


    @Bean
    public ThreadPoolExecutor threadPoolExecutor(@Value("${threadPool.coreSize}") Integer coreSize,
                                                 @Value("${threadPool.maxSize}") Integer maxSize,
                                                 @Value("${threadPool.keepAlive}") Integer keepAlive,
                                                 @Value("${threadPool.blockingQueue}") Integer queue) {
        return new ThreadPoolExecutor(coreSize, maxSize,
                keepAlive, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queue),
                Executors.defaultThreadFactory()
                , (r, e) -> log.error("线程队列已满,执行拒绝策略"));
    }
}
