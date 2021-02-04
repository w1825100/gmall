package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @program: gmall
 * @description: 聚合规格参数vo
 * @author: lgd
 * @create: 2021-01-30 01:02
 **/
@Data
public class SearchResponseAttrVo {

    private Long attrId;
    private String attrName;
    private List<String> attrValues;

}
