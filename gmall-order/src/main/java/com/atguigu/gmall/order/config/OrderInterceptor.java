package com.atguigu.gmall.order.config;


import com.atguigu.gmall.common.bean.UserInfo;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @program: gmall
 * @description: 由于购物车登录和不登录都可以访问,
 *             因此需要网关放行,微服务单独拦截获取登录状态
 * @author: lgd
 * @create: 2021-02-21 12:33
 **/
@Component
public class OrderInterceptor implements HandlerInterceptor {
    private static final ThreadLocal<UserInfo> THREAD_LOCAL=new ThreadLocal<>();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Long.valueOf(request.getHeader("userId")));
        THREAD_LOCAL.set(userInfo);
        return true;
    }

    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
       //由于线程池的存在,此处必须清除threadLocal变量
        THREAD_LOCAL.remove();
    }
}
