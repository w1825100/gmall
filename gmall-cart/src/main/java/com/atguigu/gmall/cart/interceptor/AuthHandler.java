package com.atguigu.gmall.cart.interceptor;

import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.gmall.common.bean.UserInfo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * @program: gmall
 * @description: 由于购物车登录和不登录都可以访问,
 *             因此需要网关放行,微服务单独拦截获取登录状态
 * @author: lgd
 * @create: 2021-02-21 12:33
 **/
@Component
public class AuthHandler implements HandlerInterceptor {
    private static final ThreadLocal<UserInfo> THREAD_LOCAL=new ThreadLocal<>();
    @Autowired
    JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfo userInfo = new UserInfo();
        //尝试获取用户标记,如无标记,创建标记
        String userKey = CookieUtils.getCookieValue(request, jwtProperties.getUserKey());
        if (StringUtils.isBlank(userKey)){
            userKey= UUID.randomUUID().toString();
            CookieUtils.setCookie(request,response,jwtProperties.getUserKey(),userKey, jwtProperties.getExpire());
        }
        userInfo.setUserKey(userKey);
        //尝试获取token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        if(StringUtils.isBlank(token)){
            THREAD_LOCAL.set(userInfo);
            return true;
        }
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
        Object userId = map.get("userId");
        userInfo.setUserId(Long.valueOf(userId.toString()));
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
