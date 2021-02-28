package com.atguigu.gmall.auth.service.impl;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.expection.GmallException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-19 15:33
 **/
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    GmallUmsClient umsClient;
    @Autowired
    JwtProperties jwtProperties;

    @Override
    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {
        //1.校验用户信息
        ResponseVo<UserEntity> responseVo = umsClient.queryUser(loginName, password);
        UserEntity userEntity = responseVo.getData();
        if(userEntity==null){
            throw new GmallException("用户名或密码不正确");
        }
        try {
            Map<String,Object> map=new HashMap();
            map.put("userId",userEntity.getId());
            map.put("username",userEntity.getUsername());
            String ip = IpUtils.getIpAddressAtService(request);
            //2.组装载荷信息,加入用户ip
            // 3.生成jwt token
            //4.放入cookie
            map.put("ip",ip);
            String token= JwtUtils.generateToken(map,jwtProperties.getPrivateKey(),jwtProperties.getExpire());
            CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,jwtProperties.getExpire()*60 );
            CookieUtils.setCookie(request,response,jwtProperties.getUnick(),userEntity.getNickname(),jwtProperties.getExpire()*60);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
