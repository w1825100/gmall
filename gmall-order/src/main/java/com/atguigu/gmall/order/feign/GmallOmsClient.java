package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-27 08:58
 **/
@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {
}
