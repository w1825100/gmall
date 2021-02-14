package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-15 03:16
 **/
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
