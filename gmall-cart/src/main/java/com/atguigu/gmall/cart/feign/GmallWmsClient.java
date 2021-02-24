package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-22 09:10
 **/
@FeignClient("wms-service")
public interface GmallWmsClient  extends GmallWmsApi {
}
