package com.atguigu.gmall.pms.entity;

import lombok.Data;

import java.util.Set;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-12 00:03
 **/
@Data
public class SaleAttrValueVo {
    private Long attrId;
    private String attrName;
    private Set<String> attrValues;
}
