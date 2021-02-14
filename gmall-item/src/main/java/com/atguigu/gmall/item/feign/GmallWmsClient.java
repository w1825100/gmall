package com.atguigu.gmall.item.feign;


import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-15 03:17
 **/
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {

}
