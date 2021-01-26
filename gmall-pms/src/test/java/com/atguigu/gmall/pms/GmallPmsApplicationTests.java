package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.SkuService;
import com.atguigu.gmall.pms.service.SpuDescService;
import com.zaxxer.hikari.pool.HikariPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPmsApplicationTests {

   @Autowired
    SpuDescService spuDescService;
   @Autowired
    SpuDescMapper spuDescMapper;
    @Test
    public void contextLoads() {

        SpuDescEntity search = spuDescMapper.search(27L);
        System.out.println(search);
    }

}
