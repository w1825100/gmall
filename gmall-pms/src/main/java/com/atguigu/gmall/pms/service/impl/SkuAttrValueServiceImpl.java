package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SaleAttrValueVo;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Autowired
    private AttrMapper attrMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private  SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySkuAttrValueByCategoryIdAndSkuId(Long cid, Long sid) {
        //根据分类id查询检索类型的规格参数
        List<AttrEntity> attrEntities = attrMapper.selectList(new QueryWrapper<AttrEntity>()
                .eq("category_id", cid)
                .eq("search_type", 1));
        if (CollectionUtils.isEmpty(attrEntities)) {
            return null;
        }
        List<Long> idList = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
        //查询出销售类型的规格参数
        List<SkuAttrValueEntity> skuAttrValueEntities = baseMapper.selectList(new QueryWrapper<SkuAttrValueEntity>()
                .eq("sku_id", sid)
                .in("attr_id", idList));

        return skuAttrValueEntities;
    }

    //根据spuId查询可选销售属性
    @Override
    public List<SaleAttrValueVo> querySkuSaleAttrValuesBySpuId(Long id) {
        List<SkuEntity> skuEntities = skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", id));
        if (CollectionUtils.isEmpty(skuEntities)) {
            return null;
        }
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());
        List<SkuAttrValueEntity> skuAttrValues = this.list(new QueryWrapper<SkuAttrValueEntity>().in("sku_id", skuIds));
        if (CollectionUtils.isEmpty(skuAttrValues)) {
            return null;
        }
        //将spu下所有sku按照sku_id进行分组,map结构,key为sku_id value为entity集合
        Map<Long, List<SkuAttrValueEntity>> map = skuAttrValues.stream().collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));
        List<SaleAttrValueVo> attrValueVos = new ArrayList();
        //遍历map 将SkuAttrValueEntity集合转换为saleAttrValueVo->attrId attrName AttrValues(List)
        map.forEach((attrId, skuAttrValueEntities) -> {
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            saleAttrValueVo.setAttrId(attrId);
            saleAttrValueVo.setAttrName(skuAttrValueEntities.get(0).getAttrName());
            saleAttrValueVo.setAttrValues(skuAttrValueEntities.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet()));
            attrValueVos.add(saleAttrValueVo);
        });
        return attrValueVos;
    }

    @Override
    public String querySaleAttrValuesMappingSkuIdBySpuId(Long id) {
        List<Map<String, Object>> maps = skuAttrValueMapper.querySkuSaleAttrValuesMappingBySpuId(id);
        if(CollectionUtils.isEmpty(maps)){
            return null;
        }

        Map<String, Long> attrValuesMappingSkuIdMap = maps.stream().collect(Collectors.toMap(map -> (String)map.get("attr_values"), map -> (Long)map.get("attr_id")));

        return JSON.toJSONString(attrValuesMappingSkuIdMap);
    }


}
