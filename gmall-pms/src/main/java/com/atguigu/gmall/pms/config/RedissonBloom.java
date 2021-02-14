package com.atguigu.gmall.pms.config;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
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
 * @create: 2021-02-10 23:44
 **/
@Configuration
@Slf4j
public class RedissonBloom {
    private static final String KEY_PREFIX = "gmall:pms:cates:";

    @Autowired
    CategoryService categoryService;
    @Autowired
    RedissonClient redissonClient;

    @Bean
    public RBloomFilter bloomFilter(){
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter("pms:bloom");
        System.out.println(bloomFilter);
        bloomFilter.tryInit(3000,0.03);
        List<CategoryEntity> list = categoryService.queryCategoryListByPid(0L);
        if(!CollectionUtils.isEmpty(list)){
            list.forEach(categoryEntity -> bloomFilter.add(KEY_PREFIX+"["+categoryEntity.getId()+"]"));
            log.info(KEY_PREFIX+"布隆过滤器初始化完成.长度:{}",bloomFilter.getSize());
        }
        return bloomFilter;
    }

}
