package com.atguigu.gmall.search;


import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.esmapper.GoodsRespository;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-01-28 20:35
 **/

@SpringBootTest
public class EsTest {

    @Autowired
    ElasticsearchRestTemplate restTemplate;
    @Autowired
    GoodsRespository goodsRespository;
    @Autowired
    GmallPmsClient pmsClient;
    @Autowired
    GmallWmsClient wmsClient;

    @Test
    void test() {
//        建库
        restTemplate.createIndex(Goods.class);
//        建表/生成约束
        restTemplate.putMapping(Goods.class);


        Integer pageNum = 1;
        Integer pageSize = 30;

        do {
            PageParamVo pageParamVo = new PageParamVo(pageNum, pageSize, null);
            //1.分页查询spu 获得spu集合
            ResponseVo<List<SpuEntity>> listResponseVo = pmsClient.querySpuEntities(pageParamVo);
            List<SpuEntity> spuEntities = listResponseVo.getData();
            if (CollectionUtils.isEmpty(spuEntities)) {
                break;
            }
            //2.遍历spu集合,查询每个spu下的所有sku集合
            spuEntities.forEach(spuEntity -> {
                ResponseVo<List<SkuEntity>> skuResponseVo = pmsClient.querySkusBySpuId(spuEntity.getId());
                List<SkuEntity> skuEntities = skuResponseVo.getData();
                if (!CollectionUtils.isEmpty(skuEntities)) {
                    List<Goods> goodsList = skuEntities.stream().map(
                            sku -> {
                                Goods goods = new Goods();
                                //创建时间
                                goods.setCreateTime(spuEntity.getCreateTime());

                                //sku基本信息
                                goods.setSkuId(sku.getId());
                                goods.setPrice(sku.getPrice().doubleValue());
                                goods.setTitle(sku.getTitle());
                                goods.setSubTitle(sku.getSubtitle());
                                goods.setDefaultImage(sku.getDefaultImage());
                                //库存信息
                                ResponseVo<List<WareSkuEntity>> wareSkusVo = wmsClient.queryWareSkuBySkuId(sku.getId());
                                List<WareSkuEntity> wareSkus = wareSkusVo.getData();
                                if (!CollectionUtils.isEmpty(wareSkus)) {
                                    goods.setSales(wareSkus.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a + b).get());
                                    goods.setStore(wareSkus.stream().anyMatch(wareSkuEntity ->
                                            wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0
                                    ));

                                }
                                //3.查询sku品牌信息
                                ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(sku.getBrandId());
                                BrandEntity brandEntity = brandEntityResponseVo.getData();
                                if (brandEntity != null) {
                                    goods.setBrandId(sku.getBrandId());
                                    goods.setBrandName(brandEntity.getName());
                                    goods.setLogo(brandEntity.getLogo());
                                }
                                //4.查询sku分类信息
                                ResponseVo<CategoryEntity> categoryEntityResponseVo = pmsClient.queryCategoryById(sku.getCategoryId());
                                CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                                if (categoryEntity != null) {
                                    goods.setCategoryId(sku.getCategoryId());
                                    goods.setCategoryName(categoryEntity.getName());
                                }

                                //5.根据sku分类id查询检索类型attr
                                List<SearchAttrValue> searchAttrValues = new ArrayList<SearchAttrValue>();
                                ResponseVo<List<SkuAttrValueEntity>> SkuAttrValueEntityVo = pmsClient.querySkuAttrValueByCategoryIdAndSkuId(sku.getCategoryId(), sku.getId());
                                List<SkuAttrValueEntity> SkuAttrValueEntities = SkuAttrValueEntityVo.getData();
                                ResponseVo<List<SpuAttrValueEntity>> SpuAttrValueEntityVo = pmsClient.querySpuAttrValueByCategoryIdAndSkuId(sku.getCategoryId(), sku.getSpuId());
                                List<SpuAttrValueEntity> SpuAttrValueEntities = SpuAttrValueEntityVo.getData();
                                //6.根据attr信息获得检索类型attrId
                                //7.根据attr_type分开查询基本属性和销售属性表
                                if (!CollectionUtils.isEmpty(SkuAttrValueEntities)) {
                                    List<SearchAttrValue> collects = SkuAttrValueEntities.stream().map(a -> {
                                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                                        BeanUtils.copyProperties(a, searchAttrValue);
                                        return searchAttrValue;
                                    }).collect(Collectors.toList());
                                    searchAttrValues.addAll(collects);
                                }
                                if (!CollectionUtils.isEmpty(SpuAttrValueEntities)) {
                                    List<SearchAttrValue> collects = SpuAttrValueEntities.stream().map(b -> {
                                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                                        BeanUtils.copyProperties(b, searchAttrValue);
                                        return searchAttrValue;
                                    }).collect(Collectors.toList());
                                    searchAttrValues.addAll(collects);
                                }

                                goods.setSearchAttrs(searchAttrValues);
                                return goods;
                            }
                    ).collect(Collectors.toList());
                    //8.包装Goods对象,入库
                    //finally,将转换完成的goods集合,装进es中
                    goodsRespository.saveAll(goodsList);
                }
            });
            pageSize = spuEntities.size();
            pageNum++;
        } while (pageSize == 30);

    }

}
