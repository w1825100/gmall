package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

/**
 * @program: gmall
 * @description: es商品模型
 * @author: lgd
 * @create: 2021-01-28 19:55
 **/
@Data
@Document(indexName = "goods",type = "info",shards = 3,replicas = 2)
public class Goods {

    //sku专属字段

    // 搜索列表字段
    @Id
    private Long skuId;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Keyword, index = false)
    private String subTitle;
    @Field(type = FieldType.Keyword, index = false)
    private String defaultImage;
    @Field(type = FieldType.Double)
    private Double price;

    // 排序和筛选字段
    @Field(type = FieldType.Long)
    private Long sales=0l; // 销量
    @Field(type = FieldType.Date)
    private Date createTime; // 创建时间
    @Field(type = FieldType.Boolean)
    private boolean store = false; // 是否有货

    // 聚合字段
    @Field(type = FieldType.Long)
    private Long brandId;
    @Field(type = FieldType.Keyword)
    private String brandName;
    @Field(type = FieldType.Keyword)
    private String logo;

    @Field(type = FieldType.Long)
    private Long categoryId;
    @Field(type = FieldType.Keyword)
    private String categoryName;


//    检索类型的规格参数集合
    @Field(type = FieldType.Nested)
    private List<SearchAttrValue> searchAttrs;

}
