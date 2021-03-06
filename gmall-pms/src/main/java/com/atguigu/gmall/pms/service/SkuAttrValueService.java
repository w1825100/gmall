package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.SaleAttrValueVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author lgd
 * @email lgd@atguigu.com
 * @date 2021-01-18 18:14:15
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);



    List<SkuAttrValueEntity> querySkuAttrValueByCategoryIdAndSkuId(Long cid, Long sid);

    List<SaleAttrValueVo> querySkuSaleAttrValuesBySpuId(Long id);

    String querySaleAttrValuesMappingSkuIdBySpuId(Long id);
}

