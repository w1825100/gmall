package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @program: gmall
 * @description:
 * @author: 刘广典
 * @create: 2021-01-20 18:55
 **/
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {

}
