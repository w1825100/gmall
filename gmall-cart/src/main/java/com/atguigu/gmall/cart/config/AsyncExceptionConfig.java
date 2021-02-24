package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.cart.interceptor.AsyncExeceptionHandler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-23 15:01
 **/
@Component
public class AsyncExceptionConfig implements AsyncConfigurer {
    @Autowired
    private AsyncExeceptionHandler asyncExeceptionHandler;

    @Override
    public Executor getAsyncExecutor() {
        return null;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return asyncExeceptionHandler;
    }
}
