package com.atguigu.gmall.index.config;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-08 20:30
 **/
@Configuration
public class RedissonBloom {
    private static final String KEY_PREFIX = "index:cates:";

    @Autowired
    RedissonClient redissonClient;
    @Autowired
    GmallPmsClient pmsClient;

    @Bean
    public RBloomFilter bloomFilter(){
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter("index:bloom");
        bloomFilter.tryInit(3000,0.03);
        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategoryListByPid(0L);
        List<CategoryEntity> data = listResponseVo.getData();
        if(!CollectionUtils.isEmpty(data)){
            data.forEach(categoryEntity -> {
                bloomFilter.add(KEY_PREFIX+"["+categoryEntity.getId()+"]");
            });
        }
        return bloomFilter;
    }
}
