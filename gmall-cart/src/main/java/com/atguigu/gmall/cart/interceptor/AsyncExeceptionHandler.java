package com.atguigu.gmall.cart.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-23 14:59
 **/
@Component
@Slf4j
public class AsyncExeceptionHandler implements AsyncUncaughtExceptionHandler {
    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
       log.info(throwable.getMessage());
       log.info("method:{}",method.getName());
       log.info("Obj:{}",objects);
    }
}
