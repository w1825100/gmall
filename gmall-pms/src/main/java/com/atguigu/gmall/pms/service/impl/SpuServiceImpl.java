package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.BaseAttrs;
import com.atguigu.gmall.pms.vo.Skus;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleDto;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import org.springframework.util.CollectionUtils;

@SuppressWarnings("all")
@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    private  SkuService skuService;
    @Autowired
    private SpuAttrValueService spuAttrValueService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuAttrValueService skuAttrValueService;
    @Autowired
    GmallSmsClient gmallSmsClient;
    @Autowired
    SpuDescService spuDescService;

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

    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spuvo) {
        //1.1.先保存spu表.字段如下,驼峰映射直接保存
        spuvo.setCreateTime(new Date());
        spuvo.setUpdateTime(spuvo.getCreateTime());
        this.save(spuvo);
        Long spuId = spuvo.getId();
        //1.2保存spu_desc(先判断是否需要保存)
        List<String> spuImages = spuvo.getSpuImages();
        if (!CollectionUtils.isEmpty(spuImages)) {
            spuDescService.saveSpuDesc(spuId, spuImages);
        }
        List<BaseAttrs> baseAttrs = spuvo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            spuAttrValueService.saveBatch( getCollect(spuId, baseAttrs));//把spuAttrValueVo转换为数据库bean,使用Stream流
        }

        List<Skus> skus = spuvo.getSkus();
        if (CollectionUtils.isEmpty(skus)) {
            return;
        }
        //2.1保存sku-> sku_attr_value -> sku_images
        saveSkuInfo(spuvo, spuId, skus);
        rabbitTemplate.convertAndSend("PMS_ITEM_EXCHANGE","item.insert",spuId);
    }
    public void saveSkuInfo(SpuVo spuvo, Long spuId, List<Skus> skus) {
        skus.forEach(sku -> {
            //2.1保存sku表
            sku.setSpuId(spuId);
            sku.setBrandId(spuvo.getBrandId());
            sku.setCategoryId(spuvo.getCategoryId());
            List<String> images = sku.getImages();
            if (!CollectionUtils.isEmpty(images)) {
                sku.setDefaultImage(images.get(0));
            }
            skuService.save(sku);
            Long skuId = sku.getId();
//          2.2 保存sku图片表
            saveSkuImages(sku, images, skuId);
//        2.3 保存 sku_attr_value
            List<SkuAttrValueEntity> saleAttrs = sku.getSaleAttrs();
            sveSkuAttrValue(skuId, saleAttrs);
            //3.远程调用保存sms
            SkuSaleDto skuSaleDto = new SkuSaleDto();
            BeanUtils.copyProperties(sku, skuSaleDto);
            skuSaleDto.setSkuId(sku.getId());
            gmallSmsClient.saveSales(skuSaleDto);
        });
    }

    private void sveSkuAttrValue(Long skuId, List<SkuAttrValueEntity> saleAttrs) {
        if (!CollectionUtils.isEmpty(saleAttrs)) {
            saleAttrs.forEach(skuAttrValueEntity -> skuAttrValueEntity.setSkuId(skuId));
            this.skuAttrValueService.saveBatch(saleAttrs);
        }
    }

    private void saveSkuImages(Skus sku, List<String> images, Long skuId) {
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
    }

    private List<SpuAttrValueEntity> getCollect(Long spuId, List<BaseAttrs> baseAttrs) {
        return baseAttrs.stream().map(baseAttrs1 -> {
            SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
            BeanUtils.copyProperties(baseAttrs1, spuAttrValueEntity);
            spuAttrValueEntity.setSpuId(spuId);
            return spuAttrValueEntity;
        }).collect(Collectors.toList());
    }


}
