package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-01-28 22:00
 **/

public interface GmallPmsApi {

    @PostMapping("pms/spu/json")
    @ApiOperation("分页查询")
    ResponseVo<List<SpuEntity>> querySpuEntities(@RequestBody PageParamVo paramVo);


    @GetMapping("pms/sku/spu/{sId}")
    @ApiOperation("根据spuId查询sku")
    ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable Long sId);

    @GetMapping("pms/brand/{id}")
    @ApiOperation("根据品牌id查询品牌")
    ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("pms/category/{id}")
    @ApiOperation("详情查询")
     ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);


    @GetMapping("pms/skuattrvalue/search/{cid}/{sid}")
    @ApiOperation("根据skuId和分类id查询sku属性")
   ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueByCategoryIdAndSkuId(
            @PathVariable("cid") Long cid
            ,@PathVariable("sid") Long sid);


    @GetMapping("pms/spuattrvalue/search/{cid}/{sid}")
    @ApiOperation("根据skuId和分类id查询spu属性")
    ResponseVo<List<SpuAttrValueEntity>> querySpuAttrValueByCategoryIdAndSkuId(
            @PathVariable("cid") Long cid
            ,@PathVariable("sid") Long sid);


}
