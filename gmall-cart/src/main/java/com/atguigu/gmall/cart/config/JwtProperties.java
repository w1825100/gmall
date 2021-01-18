package com.atguigu.gmall.cart.config;



import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.PublicKey;


/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-19 14:10
 **/
@Component
@ConfigurationProperties(prefix = "auth.jwt")
@Data
public class JwtProperties {
  private  String pubKeyPath;
  private  String cookieName;
  private PublicKey publicKey;
  private String userKey;


    @PostConstruct
    public void init(){
        try {
            this.publicKey= RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
