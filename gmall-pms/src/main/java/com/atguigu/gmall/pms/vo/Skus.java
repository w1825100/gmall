/**
 * Copyright 2021 bejson.com
 */
package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2021-01-20 10:37:45
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class Skus extends SkuEntity {


    //跨系统(sms)积分优惠信息
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    /**
     * 优惠生效情况[1111（四个状态位，从右到左）;
     * 0 - 无优惠，成长积分是否赠送;
     * 1 - 无优惠，购物积分是否赠送;
     * 2 - 有优惠，成长积分是否赠送
     * 3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】
     */
    private List<Integer> work;

    //跨系统(ms)满减优惠信息
    private BigDecimal fullPrice; //满多少
    private BigDecimal reducePrice;  //减多少
    private int fullAddOther;   //是否参与其他优惠

    //跨系统(sms)打折优惠信息
    private Integer fullCount;
    private BigDecimal discount;
    private int ladderAddOther; //是否叠加其他优惠[0-不可叠加，1-可叠加]
    //  sku图片列表
    private List<String> images;
    //销售属性
    private List<SkuAttrValueEntity> saleAttrs;
}
