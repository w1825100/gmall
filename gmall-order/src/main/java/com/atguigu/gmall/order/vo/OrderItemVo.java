package com.atguigu.gmall.order.vo;


import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo implements Serializable {
    private Long skuId;
    private String defaultImage;
    private String title;
    private Integer weight;
    private List<SkuAttrValueEntity> saleAttrs; // 销售属性：List<SkuAttrValueEntity>的json格式
    private BigDecimal price; // 加入购物车时的价格
    private BigDecimal count; //数量
    private Boolean store = false; // 是否有货
    private List<ItemSaleVo> sales; // 营销信息: List<ItemSaleVo>的json格式
}
