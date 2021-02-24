package com.atguigu.gmall.msg.service;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.gmall.common.expection.GmallException;
import com.atguigu.gmall.msg.config.SmsProperties;
import com.atguigu.gmall.msg.utils.FormUtils;
import com.atguigu.gmall.msg.utils.RandomUtils;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-17 01:17
 **/
@Service
@Slf4j
public class MsgService {

    @Autowired
    private SmsProperties smsProperties;
    @Autowired
    private  StringRedisTemplate redisTemplate;
    private static final String CODE_PREFIX="gmall:sms:";
    public Boolean send(String mobile) {
        String codeKey=CODE_PREFIX+mobile;
        //1.验证手机格式 2.判断是否有未过期验证码 3.生成验证码 3.1:发送 3.2:存redis
        boolean b = FormUtils.isMobile(mobile);
        if(!b){throw new GmallException("手机号不正确");
        }
        String code = RandomUtils.getFourBitRandom();
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", smsProperties.getKeyId(), smsProperties.getKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", mobile);
        request.putQueryParameter("SignName", smsProperties.getSignName());
        request.putQueryParameter("TemplateCode", smsProperties.getTemplateCode());
        HashMap<String, String> map = new HashMap<>();
        map.put("code",code);
        Gson gson=new Gson();
        String codeJson = gson.toJson(map);
        request.putQueryParameter("TemplateParam", codeJson);
        try {
            CommonResponse response = client.getCommonResponse(request);
            String data = response.getData();
            Map dataMap=gson.fromJson(data, Map.class);
            if(!dataMap.get("Code").toString().equals("OK")){
                log.error(dataMap.toString());
                throw new RuntimeException();
            }

            redisTemplate.opsForValue().set(codeKey,code,10, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new GmallException("短信发送失败");
        }
        return true;
    }
}
