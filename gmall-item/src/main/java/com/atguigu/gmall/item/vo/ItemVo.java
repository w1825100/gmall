package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.ItemGroupVo;
import com.atguigu.gmall.pms.entity.SaleAttrValueVo;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-11 23:40
 **/
@Data
public class ItemVo {
    //左上角分类集合
    private List<CategoryEntity> categories;

    //品牌信息
    private Long brandId;
    private String brandName;

    //spu信息
    private Long spuId;
    private String spuName;

    //sku信息
    private Long skuId;
    private String title;
    private String subTitle;
    private BigDecimal price;
    private String defaultImg;
    private Integer weight;

    //图片集
    private List<SkuImagesEntity> skuImages;

    //促销信息
    private List<ItemSaleVo> sales;

    //是否有货
    private boolean store;

    //销售属性集合
    private List<SaleAttrValueVo> saleAttrs;

    //当前sku销售属性
    private Map<String,String> saleAttr;

    //销售属性和skuId映射关系
    private String skusJson;

    //商品详情图片
    private List<String> spuImages;

    //分组展示规格参数
    private List<ItemGroupVo> groups;



}
