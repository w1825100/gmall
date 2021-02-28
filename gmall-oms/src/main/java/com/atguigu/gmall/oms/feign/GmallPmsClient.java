package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-27 15:26
 **/
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
