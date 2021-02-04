package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @program: gmall
 * @description: 搜索条件
 * search.gmall.com/
 * search?keyword=手机&brandId=1,2,3&categoryId=225
 * &props=4:8G-12G&props=5:128G-256G-512G&sort=1
 * @author: lgd
 * @create: 2021-01-29 22:32
 **/
@Data
public class SearchParamVo {

    //搜索字段
    private String keyword;
    //品牌id
    private List<Long> brandId;
    //分类id
    private List<Long> categoryId;
    //   规格参数
    private List<String> props;
    //排序 0默认 1价格降序 2价格升序 3销量降序 4销量升序 5新品
    private Integer sort;
    //价格区间
    private Double priceFrom;
    private  Double priceTo;
    //是否有货
    private Boolean store=false;
    //当前页
    private Integer pageNum=1;
    //每页显示多少条,写死20条
    private final Integer pageSize=20;

}
