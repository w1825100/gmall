package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-19 15:11
 **/
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {

}
