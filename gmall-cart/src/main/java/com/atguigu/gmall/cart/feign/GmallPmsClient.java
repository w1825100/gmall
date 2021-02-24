package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-22 09:09
 **/
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
