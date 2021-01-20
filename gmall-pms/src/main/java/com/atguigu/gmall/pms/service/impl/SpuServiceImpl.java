package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.vo.BaseAttrs;
import com.atguigu.gmall.pms.vo.Skus;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.dto.SkuSaleDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SpuService;
import org.springframework.util.CollectionUtils;

@SuppressWarnings("all")
@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SpuDescMapper spuDescMapper;
    @Autowired
    private SpuAttrValueService spuAttrValueService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuAttrValueService skuAttrValueService;
    @Autowired
    GmallSmsClient gmallSmsClient;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuByCidAndPage(long id, PageParamVo paramVo) {

        QueryWrapper<SpuEntity> queryWrapper = new QueryWrapper<>();
        if (id != 0) {
            queryWrapper.eq("category_id", id);
        }
        String key = paramVo.getKey();
        if (StringUtils.isNotBlank(key)) {
            queryWrapper.and(t -> t.eq("id", key).or().like("name", key));
        }
        IPage<SpuEntity> page = this.page(paramVo.getPage(), queryWrapper);
        return new PageResultVo(page);
    }

    @Override
    public void bigSave(SpuVo spuvo) {
        //1.1.先保存spu表.字段如下,驼峰映射直接保存
        /**
         *  id
         *  name
         *  category_id
         * brand_id
         * publish_status
         * create_time
         * update_time
         **/
        spuvo.setCreateTime(new Date());
        spuvo.setUpdateTime(spuvo.getCreateTime());
        this.save(spuvo);
        Long spuId = spuvo.getId();
        //1.2保存spu_desc(先判断是否需要保存)
        List<String> spuImages = spuvo.getSpuImages();
        if (!CollectionUtils.isEmpty(spuImages)) {
            SpuDescEntity spuDescEntity = new SpuDescEntity();
            spuDescEntity.setSpuId(spuId);
            spuDescEntity.setDecript(StringUtils.join(spuImages, ","));
            spuDescMapper.insert(spuDescEntity);
        }
        //1.3 spu_attr_value
        /**
         * id
         * spu_id
         * attr_id
         * attr_name
         * attr_value
         *  sort
         **/
        List<BaseAttrs> baseAttrs = spuvo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            //把spuAttrValueVo转换为数据库bean,使用Stream流
            spuAttrValueService.saveBatch(
                    baseAttrs.stream().map(baseAttrs1 -> {
                        SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                        BeanUtils.copyProperties(baseAttrs1, spuAttrValueEntity);
                        spuAttrValueEntity.setSpuId(spuId);
                        return spuAttrValueEntity;
                    }).collect(Collectors.toList())
            );
        }

        //2.1保存sku-> sku_attr_value -> sku_images
        List<Skus> skus = spuvo.getSkus();
        if (CollectionUtils.isEmpty(skus)) {
            return;
        }
        skus.forEach(sku -> {
            sku.setSpuId(spuId);
            sku.setBrandId(spuvo.getBrandId());
            sku.setCategoryId(spuvo.getCategoryId());
            List<String> images = sku.getImages();
            if (!CollectionUtils.isEmpty(images)) {
                sku.setDefaultImage(images.get(0));
            }
            skuMapper.insert(sku);
            Long skuId = sku.getId();
//          2.2 保存sku图片集
            if (!CollectionUtils.isEmpty(images)) {
                skuImagesService.saveBatch(
                        images
                                .stream()
                                .map(img -> {
                                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                                    skuImagesEntity.setSkuId(skuId);
                                    skuImagesEntity.setUrl(img);
                                    skuImagesEntity.setDefaultStatus(StringUtils.equals(sku.getDefaultImage(), img) ? 1 : 0);
                                    return skuImagesEntity;
                                })
                                .collect(Collectors.toList())
                );
            }
//        2.3 保存 sku_attr_value
            List<SkuAttrValueEntity> saleAttrs = sku.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)) {
                saleAttrs.forEach(skuAttrValueEntity -> skuAttrValueEntity.setSkuId(skuId));
                this.skuAttrValueService.saveBatch(saleAttrs);
            }

            SkuSaleDto skuSaleDto = new SkuSaleDto();
            //3.保存sms
            BeanUtils.copyProperties(sku, skuSaleDto);
            skuSaleDto.setSkuId(sku.getId());
            gmallSmsClient.saveSales(skuSaleDto);
        });


    }

}
