package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-22 09:09
 **/
@FeignClient("sms-service")
public interface GmallSmsClient  extends GmallSmsApi {
}
