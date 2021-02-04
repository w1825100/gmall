package com.atguigu.gmall.search.pojo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-01-30 01:00
 **/
@Data
public class SearchResponseVo {

    //过滤
    private List<BrandEntity> brands;
    private List<CategoryEntity> categories;
    private List<SearchResponseAttrVo> filters;

    //分页
    private Integer pageNum;
    private Integer pageSize;
    private Long total;

    //数据
    private List<Goods> goodsList;

}
