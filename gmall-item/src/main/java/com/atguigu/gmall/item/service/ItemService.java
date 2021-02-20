package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.expection.ItemException;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;

import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.sun.media.jfxmedia.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-15 03:14
 **/
@Service
@Slf4j
public class ItemService {

    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private TemplateEngine templateEngine;

    public ItemVo loadData(Long skuId) {
        log.info("线程池信息:核心数:{},存活时间{},阻塞队列:{}",
                threadPoolExecutor.getCorePoolSize(),threadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS),
                threadPoolExecutor.getQueue().remainingCapacity()
        );
        ItemVo itemVo = new ItemVo();
        //skuFuture根任务
        CompletableFuture<SkuEntity> skuFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(skuId);
            SkuEntity sku = skuEntityResponseVo.getData();
            if (sku == null) {
                throw new ItemException("该商品对应的页面不存在");
            }

            //1.sku基础信息
            itemVo.setSkuId(skuId);
            itemVo.setTitle(sku.getTitle());
            itemVo.setSubTitle(sku.getSubtitle());
            itemVo.setPrice(sku.getPrice());
            itemVo.setWeight(sku.getWeight());
            itemVo.setDefaultImg(sku.getDefaultImage());
            return sku;
        }, threadPoolExecutor);
        //skuFuture.子任务1 获得上个任务结果集,无需给下个任务返回结果集
        CompletableFuture<Void> categoryFuture = skuFuture.thenAcceptAsync(sku -> {
            ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryLevel123CategoriesByCid3(sku.getCategoryId());
            List<CategoryEntity> categoryEntities = listResponseVo.getData();
            //2.分类信息
            itemVo.setCategories(categoryEntities);
        }, threadPoolExecutor);
        //skuFuture.子任务2 获得上个任务结果集,无需给下个任务返回结果集
        CompletableFuture<Void> brandFuture = skuFuture.thenAcceptAsync(sku -> {
            ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(sku.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            if (brandEntity != null) {
                //3.品牌信息
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }, threadPoolExecutor);
        //skuFuture.子任务3 获得上个任务结果集,无需给下个任务返回结果集
        CompletableFuture<Void> spuFuture = skuFuture.thenAcceptAsync(sku -> {
            ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(sku.getSpuId());
            SpuEntity spu = spuEntityResponseVo.getData();
            if (spu != null) {
                //4spu信息
                itemVo.setSpuId(spu.getId());
                itemVo.setSpuName(spu.getName());
            }
        }, threadPoolExecutor);
        //根任务,独立运行 仅运行,无返回结果
        CompletableFuture<Void> skuImgFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuImagesEntity>> listResponseVo1 = pmsClient.querySkuImagesBySkuId(skuId);
            List<SkuImagesEntity> skuimages = listResponseVo1.getData();
            //5sku图片
            itemVo.setSkuImages(skuimages);
        }, threadPoolExecutor);

        //根任务,独立运行 仅运行,无返回结果
        CompletableFuture<Void> saleFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<ItemSaleVo>> salesResponseVo = smsClient.querySalesBySkuId(skuId);
            List<ItemSaleVo> sales = salesResponseVo.getData();
            //6营销信息
            itemVo.setSales(sales);
        }, threadPoolExecutor);

        //根任务,独立运行 仅运行,无返回结果
        CompletableFuture<Void> wareSkuFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<WareSkuEntity>> wareRespinseVo = wmsClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareRespinseVo.getData();
            if (CollectionUtils.isEmpty(wareSkuEntities)) {
                //7库存信息
                itemVo.setStore(false);
            } else {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
        }, threadPoolExecutor);

        //skuFuture.子任务4 获得上个任务结果集,无需给下个任务返回结果集
        CompletableFuture<Void> spusalesFuture = skuFuture.thenAcceptAsync(sku -> {
            ResponseVo<List<SaleAttrValueVo>> saleAttrValues = pmsClient.querySkuSaleAttrValuesBySpuId(sku.getSpuId());
            List<SaleAttrValueVo> saleAttrValueVos = saleAttrValues.getData();
            //8spu销售属性
            itemVo.setSaleAttrs(saleAttrValueVos);
        }, threadPoolExecutor);


        //根任务,独立运行 仅运行,无返回结果
        CompletableFuture<Void> skusalesFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuAttrValueEntity>> responseVo = pmsClient.querySaleAttrValueBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = responseVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                Map<Long, String> collect = skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
                //9sku销售属性
                itemVo.setSaleAttr(collect);
            }
        }, threadPoolExecutor);
        //skuFuture.子任务5 获得上个任务结果集,无需给下个任务返回结果集
        CompletableFuture<Void> skusJsonMappingFuture = skuFuture.thenAcceptAsync(sku -> {
            ResponseVo<String> stringResponseVo = pmsClient.querySaleAttrValuesMappingSkuIdBySpuId(sku.getSpuId());
            //10sku与销售属性映射关系
            itemVo.setSkusJson(stringResponseVo.getData());
        }, threadPoolExecutor);

        //skuFuture.子任务6 获得上个任务结果集,无需给下个任务返回结果集
        CompletableFuture<Void> groupsFuture = skuFuture.thenAcceptAsync(sku -> {
            ResponseVo<List<ItemGroupVo>> resp = pmsClient.queryGroupsWithAttrsAndValuesByCategoryIdAndSpuIdAndSkuId(sku.getCategoryId(), sku.getId(), sku.getSpuId());
            List<ItemGroupVo> itemGroupVos = resp.getData();
            //11规格参数组及组下信息
            itemVo.setGroups(itemGroupVos);
        }, threadPoolExecutor);

        //skuFuture.子任务7 获得上个任务结果集,无需给下个任务返回结果集
        CompletableFuture<Void> spuDescFuture = skuFuture.thenAcceptAsync(sku -> {
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = pmsClient.querySpuDescById(sku.getSpuId());
            SpuDescEntity spuDesc = spuDescEntityResponseVo.getData();
            if (spuDesc != null) {
                List<String> strings = Arrays.asList(StringUtils.split(spuDesc.getDecript(), ","));
                //12 spu desc
                itemVo.setSpuImages(strings);
            }
        }, threadPoolExecutor);

        CompletableFuture.allOf(categoryFuture, brandFuture, spuFuture,
                skuImgFuture, saleFuture, wareSkuFuture,
                spusalesFuture, skusalesFuture, skusJsonMappingFuture,
                groupsFuture, spuDescFuture).join();

        return itemVo;
    }

    private  void creatHtml(Long skuId){
        ItemVo itemVo = this.loadData(skuId);
        Context context =new Context();
        context.setVariable("itemVo",itemVo);
        try(PrintWriter printWriter=new PrintWriter(new File("d:\\html\\"+skuId+".html")))
        {
            templateEngine.process("item",context,printWriter);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void asyncExecute(Long skuId){
        threadPoolExecutor.execute(()->{
            creatHtml(skuId);
        });
    }
}
