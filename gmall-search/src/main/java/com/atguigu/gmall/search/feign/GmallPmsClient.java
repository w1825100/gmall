package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-01-28 22:54
 **/
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {

}
