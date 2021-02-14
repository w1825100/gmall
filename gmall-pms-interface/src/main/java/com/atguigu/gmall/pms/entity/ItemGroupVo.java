package com.atguigu.gmall.pms.entity;

import lombok.Data;

import java.util.List;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-12 00:16
 **/
@Data
public class ItemGroupVo {
    private  String name;
    private  Long id;
    private List<AttrValueVo> attrs;
}
