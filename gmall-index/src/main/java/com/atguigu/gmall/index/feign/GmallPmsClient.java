package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-05 02:16
 **/
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {

}
