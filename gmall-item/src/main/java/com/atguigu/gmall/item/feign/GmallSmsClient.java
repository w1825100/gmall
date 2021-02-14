package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-15 03:17
 **/
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
