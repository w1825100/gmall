package com.atguigu.gmall.pms;


import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.SpuDescService;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import javax.sql.DataSource;


@SpringBootTest
public class GmallPmsApplicationTests {

   @Autowired
    SpuDescService spuDescService;
   @Autowired
    SpuDescMapper spuDescMapper;
    @Autowired
    DataSource dataSource;
    @Test
     void contextLoads0() {

        SpuDescEntity search = spuDescMapper.search(27L);
        System.out.println(dataSource.getClass());
        System.out.println(search);
    }
    @Test
     void contextLoads1() {

        SpuDescEntity search = spuDescMapper.search(27L);
        System.out.println(dataSource.getClass());
        System.out.println(search);
    }


}
