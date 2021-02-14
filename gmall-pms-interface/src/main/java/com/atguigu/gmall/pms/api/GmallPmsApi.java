package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-01-28 22:00
 **/

public interface GmallPmsApi {
///////////////////*/index*/////////////////////
    @PostMapping("pms/spu/json")
    @ApiOperation("分页查询")
    ResponseVo<List<SpuEntity>> querySpuEntities(@RequestBody PageParamVo paramVo);

    @GetMapping("pms/spu/{id}")
    @ApiOperation("根据spuId查询spu详情")
     ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    @GetMapping("pms/sku/spu/{sId}")
    @ApiOperation("根据spuId查询sku")
    ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable Long sId);

    @GetMapping("pms/brand/{id}")
    @ApiOperation("根据品牌id查询品牌")
    ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("pms/category/{id}")
    @ApiOperation("根据分类id查询分类")
     ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);


    @GetMapping("pms/skuattrvalue/search/{cid}/{sid}")
    @ApiOperation("根据skuId和分类id查询sku属性")
   ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueByCategoryIdAndSkuId(
            @PathVariable("cid") Long cid
            ,@PathVariable("sid") Long sid);

    @GetMapping("pms/spuattrvalue/search/{cid}/{sid}")
    @ApiOperation("根据spuId和分类id查询spu属性")
    ResponseVo<List<SpuAttrValueEntity>> querySpuAttrValueByCategoryIdAndSkuId(
            @PathVariable("cid") Long cid
            ,@PathVariable("sid") Long sid);

    @GetMapping("pms/category/parent/{id}")
    @ApiOperation("根据父分类id查询子分类")
     ResponseVo<List<CategoryEntity>> queryCategoryListByPid(@PathVariable("id") Long id);

    @ApiOperation("根据一级分类id查询二三级分类")
    @GetMapping("pms/category/lv2/subs/{pid}")
    ResponseVo<List<CategoryEntity>> getSubsCategories(@PathVariable Long pid);

    ///////////////////*/item*/////////////////////
    @GetMapping("pms/sku/{id}")
    @ApiOperation("根据skuId查询sku")
    ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    @ApiOperation("根据三级级分类id查询一二三级分类集合")
    @GetMapping("pms/category/all/{id}")
    ResponseVo<List<CategoryEntity>> queryLevel123CategoriesByCid3(@PathVariable Long id);

    @GetMapping("pms/skuimages/sku/{id}")
    @ApiOperation("根据skuId查询所属图片集")
    ResponseVo<List<SkuImagesEntity>> querySkuImagesBySkuId(@PathVariable Long id);

    @GetMapping("pms/skuattrvalue/spu/{id}")
    @ApiOperation("根据spuId查询所属sku销售属性")
     ResponseVo<List<SaleAttrValueVo>> querySkuSaleAttrValuesBySpuId(@PathVariable Long id);

    @GetMapping("pms/skuattrvalue/sku/{id}")
    @ApiOperation("根据skuId查询sku销售属性")
   ResponseVo<List<SkuAttrValueEntity>> querySaleAttrValueBySkuId(@PathVariable Long id);

    @GetMapping("pms/skuattrvalue/mapping/spu/{id}")
    @ApiOperation("查询spu下sku与销售属性映射关系")
    ResponseVo<String> querySaleAttrValuesMappingSkuIdBySpuId(@PathVariable Long id);

    @GetMapping("pms/spudesc/{spuId}")
    @ApiOperation("详情查询")
     ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/attrgroup/withattr/value/category/{cid}")
    @ApiOperation("根据分类id,spuId,skuId,查询分类下的分组集合,分组下的规格参数集合")
    ResponseVo<List<ItemGroupVo>> queryGroupsWithAttrsAndValuesByCategoryIdAndSpuIdAndSkuId(
            @PathVariable("cid") Long cid, @RequestParam("skuId") Long skuId, @RequestParam("spuId") Long spuId);
}
