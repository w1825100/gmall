package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.esmapper.GoodsRespository;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: gmall
 * @description: 消息队列消费方
 * @author: lgd
 * @create: 2021-02-03 21:05
 **/
@Component
@Slf4j
public class GoodsListener {

    @Autowired
    GmallPmsClient pmsClient;
    @Autowired
    GmallWmsClient wmsClient;
    @Autowired
    GoodsRespository goodsRespository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "SEARCH_INSERT_QUEUE", durable = "true"),
            exchange = @Exchange(value = "PMS_ITEM_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.insert"}
    ))
    public void listener(Long spuId, Channel channel, Message message) throws IOException {
      log.info("搜索监听到mq消息.....:{}",spuId);
        if (spuId == null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(spuId);
        SpuEntity spuEntity = spuEntityResponseVo.getData();
        if (spuEntity == null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        ResponseVo<List<SkuEntity>> skuResponseVo = pmsClient.querySkusBySpuId(spuId);
        List<SkuEntity> skuEntities = skuResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuEntities)) {
            List<Goods> goodsList = skuEntities.stream().map(
                    sku -> {
                        Goods goods = new Goods();
                        //创建时间
                        goods.setCreateTime(spuEntity.getCreateTime());

                        //sku基本信息
                        goods.setSkuId(sku.getId());
                        goods.setPrice(sku.getPrice().doubleValue());
                        goods.setTitle(sku.getTitle());
                        goods.setSubTitle(sku.getSubtitle());
                        goods.setDefaultImage(sku.getDefaultImage());
                        //库存信息
                        ResponseVo<List<WareSkuEntity>> wareSkusVo = wmsClient.queryWareSkuBySkuId(sku.getId());
                        List<WareSkuEntity> wareSkus = wareSkusVo.getData();
                        if (!CollectionUtils.isEmpty(wareSkus)) {
                            goods.setSales(wareSkus.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a + b).get());
                            goods.setStore(wareSkus.stream().anyMatch(wareSkuEntity ->
                                    wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0
                            ));

                        }
                        //3.查询sku品牌信息
                        ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(sku.getBrandId());
                        BrandEntity brandEntity = brandEntityResponseVo.getData();
                        if (brandEntity != null) {
                            goods.setBrandId(sku.getBrandId());
                            goods.setBrandName(brandEntity.getName());
                            goods.setLogo(brandEntity.getLogo());
                        }
                        //4.查询sku分类信息
                        ResponseVo<CategoryEntity> categoryEntityResponseVo = pmsClient.queryCategoryById(sku.getCategoryId());
                        CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                        if (categoryEntity != null) {
                            goods.setCategoryId(sku.getCategoryId());
                            goods.setCategoryName(categoryEntity.getName());
                        }
                        //5.根据sku分类id查询检索类型attr
                        List<SearchAttrValue> searchAttrValues = new ArrayList<SearchAttrValue>();
                        ResponseVo<List<SkuAttrValueEntity>> SkuAttrValueEntityVo = pmsClient.querySkuAttrValueByCategoryIdAndSkuId(sku.getCategoryId(), sku.getId());
                        List<SkuAttrValueEntity> SkuAttrValueEntities = SkuAttrValueEntityVo.getData();
                        ResponseVo<List<SpuAttrValueEntity>> SpuAttrValueEntityVo = pmsClient.querySpuAttrValueByCategoryIdAndSkuId(sku.getCategoryId(), sku.getSpuId());
                        List<SpuAttrValueEntity> SpuAttrValueEntities = SpuAttrValueEntityVo.getData();
                        //6.根据attr信息获得检索类型attrId
                        //7.根据attr_type分开查询基本属性和销售属性表
                        if (!CollectionUtils.isEmpty(SkuAttrValueEntities)) {
                            List<SearchAttrValue> collects = SkuAttrValueEntities.stream().map(a -> {
                                SearchAttrValue searchAttrValue = new SearchAttrValue();
                                BeanUtils.copyProperties(a, searchAttrValue);
                                return searchAttrValue;
                            }).collect(Collectors.toList());
                            searchAttrValues.addAll(collects);
                        }
                        if (!CollectionUtils.isEmpty(SpuAttrValueEntities)) {
                            List<SearchAttrValue> collects = SpuAttrValueEntities.stream().map(b -> {
                                SearchAttrValue searchAttrValue = new SearchAttrValue();
                                BeanUtils.copyProperties(b, searchAttrValue);
                                return searchAttrValue;
                            }).collect(Collectors.toList());
                            searchAttrValues.addAll(collects);
                        }

                        goods.setSearchAttrs(searchAttrValues);
                        return goods;
                    }
            ).collect(Collectors.toList());
            //8.包装Goods对象,入库
            //finally,将转换完成的goods集合,装进es中
            goodsRespository.saveAll(goodsList);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
