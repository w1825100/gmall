package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Resource
    AttrMapper attrMapper;

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

}
