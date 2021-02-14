package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.baomidou.mybatisplus.extension.api.R;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


@Service("attrGroupService")
@SuppressWarnings("all")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private  AttrMapper attrMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrGroupEntity> queryAttrGroupAndAttrBycatId(long catId) {

        QueryWrapper<AttrGroupEntity> qr=new QueryWrapper<>();
        qr.eq("category_id", catId);
        List<AttrGroupEntity> list = baseMapper.selectList(qr);
        if(CollectionUtils.isEmpty(list)){
            return null;
        }

        list.forEach(AttrGroupEntity->{
            QueryWrapper<AttrEntity> queryWrapper1=new QueryWrapper<>();
            List<AttrEntity> attrEntities=attrMapper.selectList(queryWrapper1
                    .eq("group_id",AttrGroupEntity .getId())
                    .eq("type",1));
            AttrGroupEntity.setAttrEntities(attrEntities);
        });
        return list;
    }

    @Override
    public List<ItemGroupVo> queryGroupsWithAttrsAndValuesByCategoryIdAndSpuIdAndSkuId(Long cid, Long skuId, Long spuId) {
        //1.根据分类id查询分组集合
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
        if(CollectionUtils.isEmpty(attrGroupEntities)){
            return null;
        }
        //2.遍历分组集合,将分组集合变为groupVo集合
        List<ItemGroupVo> itemGroupVos = attrGroupEntities.stream().map(attrGroupEntity -> {
            ItemGroupVo itemGroupVo = new ItemGroupVo();
            itemGroupVo.setId(attrGroupEntity.getId());
            itemGroupVo.setName(attrGroupEntity.getName());
            //3.查询组下的规格参数
            List<AttrEntity> attrEntities = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()));
            if(!CollectionUtils.isEmpty(attrEntities)){
                List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
                List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().in("attr_id", attrIds).eq("sku_id", skuId));
                List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueMapper.selectList(new QueryWrapper<SpuAttrValueEntity>().in("attr_id", attrIds).eq("spu_id", spuId));
                List<AttrValueVo> attrValueVos=new ArrayList<>();
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                    attrValueVos.addAll(skuAttrValueEntities.stream().map(
                            skuAttrValueEntity -> {
                                AttrValueVo attrValueVo = new AttrValueVo();
                                BeanUtils.copyProperties(skuAttrValueEntity,attrValueVo);
                                return attrValueVo;
                            }
                    ).collect(Collectors.toList()));
                }
                if(!CollectionUtils.isEmpty(spuAttrValueEntities)){

                    attrValueVos.addAll(spuAttrValueEntities.stream().map(
                            spuAttrValueEntity -> {
                                AttrValueVo attrValueVo = new AttrValueVo();
                                BeanUtils.copyProperties(spuAttrValueEntity,attrValueVo);
                                return attrValueVo;
                            }
                    ).collect(Collectors.toList()));
                }
                itemGroupVo.setAttrs(attrValueVos);
            }
            return itemGroupVo;
        }).collect(Collectors.toList());
        return itemGroupVos;
    }
}
