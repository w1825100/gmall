package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.dto.SkuSaleDto;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@SuppressWarnings("all")
@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    SkuFullReductionMapper skuFullReductionMapper;
    @Autowired
    SkuLadderMapper skuLadderMapper;


    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSales(SkuSaleDto skuSaleDto) {
        //1保存sku_bounds表
        SkuBoundsEntity skuBoundsEntity=new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleDto,skuBoundsEntity);
        List<Integer> work = skuSaleDto.getWork();
        if(!CollectionUtils.isEmpty(work)){
            int sum=0;
            for (int i = work.size()-1; i >=0 ; i--) {
                Double pow = Math.pow(2, i);
                int j = pow.intValue();
               sum+=work.get(i)*j;
            }
            skuBoundsEntity.setWork(sum);
        }
        save(skuBoundsEntity);

        //2保存 sku_full_reduction表
        SkuFullReductionEntity skuFullReductionEntity=new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleDto,skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuSaleDto.getFullAddOther());
        skuFullReductionMapper.insert(skuFullReductionEntity);

        //3保存 sku_ladder表
        SkuLadderEntity skuLadderEntity=new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleDto,skuLadderEntity);
        skuLadderEntity.setAddOther(skuSaleDto.getLadderAddOther());
        skuLadderMapper.insert(skuLadderEntity);
    }

}
