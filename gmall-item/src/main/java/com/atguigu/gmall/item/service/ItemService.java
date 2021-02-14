package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.expection.ItemException;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;

import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-15 03:14
 **/
@Service
public class ItemService {

    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallSmsClient smsClient;





    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();

        ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(skuId);
        SkuEntity sku = skuEntityResponseVo.getData();
        if(sku==null){
            throw new ItemException("该商品对应的页面不存在");
        }

        //1.sku基础信息
        itemVo.setSkuId(skuId);
        itemVo.setTitle(sku.getTitle());
        itemVo.setSubTitle(sku.getSubtitle());
        itemVo.setPrice(sku.getPrice());
        itemVo.setWeight(sku.getWeight());
        itemVo.setDefaultImg(sku.getDefaultImage());

        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryLevel123CategoriesByCid3(sku.getCategoryId());
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        //2.分类信息
        itemVo.setCategories(categoryEntities);

        ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(sku.getBrandId());
        BrandEntity brandEntity = brandEntityResponseVo.getData();
        if(brandEntity!=null){
            //3.品牌信息
            itemVo.setBrandId(brandEntity.getId());
            itemVo.setBrandName(brandEntity.getName());
        }

        ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(sku.getSpuId());
        SpuEntity spu = spuEntityResponseVo.getData();
        if(spu!=null){
            //4spu信息
            itemVo.setSpuId(spu.getId());
            itemVo.setSpuName(spu.getName());
        }
        ResponseVo<List<SkuImagesEntity>> listResponseVo1 = pmsClient.querySkuImagesBySkuId(sku.getId());
        List<SkuImagesEntity> skuimages = listResponseVo1.getData();
        //5sku图片
        itemVo.setSkuImages(skuimages);

        ResponseVo<List<ItemSaleVo>> salesResponseVo = smsClient.querySalesBySkuId(sku.getId());
        List<ItemSaleVo> sales = salesResponseVo.getData();

        //6营销信息
        itemVo.setSales(sales);


        ResponseVo<List<WareSkuEntity>> wareRespinseVo = wmsClient.queryWareSkuBySkuId(sku.getId());
        List<WareSkuEntity> wareSkuEntities = wareRespinseVo.getData();
        if(CollectionUtils.isEmpty(wareSkuEntities)){
            //7库存信息
            itemVo.setStore(false);
        }else{
            itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()>0));
        }

        ResponseVo<List<SaleAttrValueVo>> saleAttrValues= pmsClient.querySkuSaleAttrValuesBySpuId(sku.getSpuId());
        List<SaleAttrValueVo> saleAttrValueVos = saleAttrValues.getData();
        //8spu销售属性
        itemVo.setSaleAttrs(saleAttrValueVos);


        ResponseVo<List<SkuAttrValueEntity>> responseVo = pmsClient.querySaleAttrValueBySkuId(sku.getId());
        List<SkuAttrValueEntity> skuAttrValueEntities = responseVo.getData();
        if(!CollectionUtils.isEmpty(skuAttrValueEntities)){
            Map<Long, String> collect = skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
            //9sku销售属性
            itemVo.setSaleAttr(collect);
        }

        ResponseVo<String> stringResponseVo = pmsClient.querySaleAttrValuesMappingSkuIdBySpuId(sku.getSpuId());
        //10sku与销售属性映射关系
        itemVo.setSkusJson(stringResponseVo.getData());


        ResponseVo<List<ItemGroupVo>> resp = pmsClient.queryGroupsWithAttrsAndValuesByCategoryIdAndSpuIdAndSkuId(sku.getCategoryId(), sku.getId(), sku.getSpuId());
        List<ItemGroupVo> itemGroupVos = resp.getData();
        //11规格参数组及组下信息
        itemVo.setGroups(itemGroupVos);


        ResponseVo<SpuDescEntity> spuDescEntityResponseVo = pmsClient.querySpuDescById(sku.getSpuId());
        SpuDescEntity spuDesc = spuDescEntityResponseVo.getData();
        if (spuDesc!=null){
            List<String> strings = Arrays.asList(StringUtils.split(spuDesc.getDecript(), ","));
            //12 spu desc
            itemVo.setSpuImages(strings);
        }


        return itemVo;
    }
}
