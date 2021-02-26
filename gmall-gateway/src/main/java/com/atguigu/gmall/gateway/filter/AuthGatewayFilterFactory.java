package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.gateway.config.JwtProperties;
import com.atguigu.gmall.gateway.utils.IpUtils;
import com.atguigu.gmall.gateway.utils.JwtUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.apache.commons.lang3.StringUtils;


import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-19 18:21
 **/

/**
*   @desc
 *   该类以"XXX"GatewayFilterFactory开头,
 *   会被网关以XXX作为过滤条件进行配置文件中配置了filters: - XXX=/**
 *   的服务匹配进行拦截,没配置的则不拦截
*   @auth lgd
*   @Date 2021/2/25 13:59
**/
@Component
@Slf4j
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathConfig> {

    @Autowired
    JwtProperties jwtProperties;

    public AuthGatewayFilterFactory() {
        super(PathConfig.class);
    }

    @Override
    public GatewayFilter apply(PathConfig config) {
        return (exchange, chain) -> {
            List<String> paths = config.paths;
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            String reqPath = request.getURI().getPath();
            log.info("客户请求地址:{}",reqPath);
            if (!CollectionUtils.isEmpty(paths)) {
                if (!paths.stream().anyMatch(path -> reqPath.startsWith(path))) {
                    return chain.filter(exchange);
                }
            }
            HttpHeaders headers = request.getHeaders();
            String token = headers.getFirst("token");
            log.info("请求头中token:{}",token);
            if (StringUtils.isBlank(token)) {
                MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                if (!CollectionUtils.isEmpty(cookies)&&cookies.containsKey(jwtProperties.getCookieName())) {
                    HttpCookie cookie = cookies.getFirst(jwtProperties.getCookieName());
                    token = cookie.getValue();
                    log.info("cookie中token:{}",token);
                }
            }
            if (StringUtils.isBlank(token)) {
                log.info("未获取到有效token,跳转首页...");
                //没获取到token,拦截
                response.setStatusCode(HttpStatus.SEE_OTHER);
                response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                return response.setComplete();
            }

            try {
                //获取到token.判断客户机ip与jwt中ip是否一致
                Map<String, Object> info = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
                String ipInJwt = info.get("ip").toString();
                log.info("jwtIp:{}",ipInJwt);
                String ipAddress = IpUtils.getIpAddressAtGateway(request);
               log.info("真实ip:{}",ipAddress);
                if (!StringUtils.equals(ipInJwt, ipAddress)) {
                    log.info("用户ip已改变,需要重新登录...原始ip:{},目前ip:{}",ipInJwt,ipAddress);
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    return response.setComplete();
                }
                //重新构建请求头
                ServerHttpRequest newRequest = request.mutate().header("userId", info.get("userId").toString()).build();
                //重新构建请求对象
                exchange.mutate().request(request).build();
                String userId = newRequest.getHeaders().getFirst("userId");
                log.info("userId:{}",userId);
                return chain.filter(exchange);
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatusCode(HttpStatus.SEE_OTHER);
                response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                return response.setComplete();
            }
        };
    }
    //参数顺序
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("paths");
    }
    //参数类型
    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    //匿名内部类用于接收配置文件参数
    @Data
    public static class PathConfig {
        private List<String> paths;
    }

}
