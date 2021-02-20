package com.atguigu.gmall.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.sql.SQLOutput;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-19 18:13
 **/
@Component
@Slf4j
public class MyGlobalFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("全局拦截....");
        System.out.println(exchange.getRequest().toString());
        String host = exchange.getRequest().getHeaders().getFirst("Host");
        String origin = exchange.getRequest().getHeaders().getFirst("Origin");
        log.info("host:{}",host);
        log.info("origin:{}",origin);
        return chain.filter(exchange);
    }
}
