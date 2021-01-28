package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-01-28 22:56
 **/
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {

}
