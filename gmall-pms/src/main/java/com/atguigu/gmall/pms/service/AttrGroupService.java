package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.ItemGroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;

import java.util.List;

/**
 * 属性分组
 *
 * @author lgd
 * @email lgd@atguigu.com
 * @date 2021-01-18 18:14:15
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrGroupEntity> queryAttrGroupAndAttrBycatId(long catId);

    List<ItemGroupVo> queryGroupsWithAttrsAndValuesByCategoryIdAndSpuIdAndSkuId(Long cid, Long skuId, Long spuId);
}

