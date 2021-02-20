package com.atguigu.gmall.msg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aliyun.sms")
@Data
public class SmsProperties {

    private String regionId;
    private String keyId;
    private String keySecret;
    private String templateCode;
    private String signName;

}
