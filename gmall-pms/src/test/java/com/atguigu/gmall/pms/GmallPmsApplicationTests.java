package com.atguigu.gmall.pms;


import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SpuDescService;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.configuration.beanutils.DefaultBeanFactory;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;


@SpringBootTest
public class GmallPmsApplicationTests {

    @Autowired
    SkuAttrValueService skuAttrValueService;
    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads0() {

        List<SkuEntity> skus = skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", 7L));
        List<Long> skuIds = skus.stream().map(SkuEntity::getId).collect(Collectors.toList());
        List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueService.list(new QueryWrapper<SkuAttrValueEntity>().in("sku_id", skuIds));
        Map<Long, List<SkuAttrValueEntity>> collect = skuAttrValueEntities.stream().collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));
        System.out.println(collect);

    }

    //${}有sql注入风险
    @Test
    void contextLoads1() {
        List<SkuEntity> skuEntities = skuMapper.querySkuByIdNoStatement("1 OR 1=1");
        System.out.println(skuEntities);
    }

    @Test
    void test3() {
        List<Map<String, Object>> maps = skuAttrValueMapper.querySkuSaleAttrValuesMappingBySpuId(7L);
        System.out.println(maps);
    }

    @Test
    void test4() {
        rabbitTemplate.convertAndSend("PMS_ITEM_EXCHANGE", "item.insert", 15L);
        rabbitTemplate.convertAndSend("PMS_ITEM_EXCHANGE","item.update",15L);
    }


    public static void main(String[] args) {
        int i = new Random().nextInt(10);
        System.out.println(i);
    }
}
