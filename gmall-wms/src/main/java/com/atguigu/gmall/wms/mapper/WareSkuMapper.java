package com.atguigu.gmall.wms.mapper;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 *
 * @author lgd
 * @email lgd@atguigu.com
 * @date 2021-01-18 21:27:45
 */
@Mapper
public interface WareSkuMapper extends BaseMapper<WareSkuEntity> {

    List<WareSkuEntity> check(@Param("skuId") Long skuId, @Param("count") Integer count);

    int lock(Long id, Integer count);

    int unlock(@Param("id") Long wareSkuId, @Param("count") Integer count);

    int minus(@Param("id") Long id, @Param("count") Integer count);
}
