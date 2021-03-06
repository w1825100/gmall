package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.entity.SaleAttrValueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * sku销售属性&值
 *
 * @author lgd
 * @email lgd@atguigu.com
 * @date 2021-01-18 18:14:15
 */
@Api(tags = "sku销售属性&值 管理")
@RestController
@RequestMapping("pms/skuattrvalue")
public class SkuAttrValueController {

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @GetMapping("mapping/spu/{id}")
    @ApiOperation("查询spu下sku与销售属性映射关系")
    public ResponseVo<String> querySaleAttrValuesMappingSkuIdBySpuId(@PathVariable Long id){
        String json=skuAttrValueService.querySaleAttrValuesMappingSkuIdBySpuId(id);
        return ResponseVo.ok(json);
    }

    @GetMapping("sku/{id}")
    @ApiOperation("根据skuId查询sku销售属性")
    public ResponseVo<List<SkuAttrValueEntity>> querySaleAttrValueBySkuId(@PathVariable Long id){
       List<SkuAttrValueEntity> skuAttrValueEntities= skuAttrValueService.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id",id));
        return ResponseVo.ok(skuAttrValueEntities);
    }

@GetMapping("spu/{id}")
@ApiOperation("根据spuId查询所属sku销售属性")
public ResponseVo<List<SaleAttrValueVo>> querySkuSaleAttrValuesBySpuId(@PathVariable Long id){
   List<SaleAttrValueVo>  list=skuAttrValueService.querySkuSaleAttrValuesBySpuId(id);
   return ResponseVo.ok(list);
}


    @GetMapping("search/{cid}/{sid}")
    @ApiOperation("根据skuId和分类id查询sku属性")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueByCategoryIdAndSkuId(
            @PathVariable("cid") Long cid
            ,@PathVariable("sid") Long sid){

        List<SkuAttrValueEntity> skuAttrValueEntities= skuAttrValueService.querySkuAttrValueByCategoryIdAndSkuId(cid,sid);

        return ResponseVo.ok(skuAttrValueEntities);
    }





    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySkuAttrValueByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = skuAttrValueService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuAttrValueEntity> querySkuAttrValueById(@PathVariable("id") Long id){
		SkuAttrValueEntity skuAttrValue = skuAttrValueService.getById(id);

        return ResponseVo.ok(skuAttrValue);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuAttrValueEntity skuAttrValue){
		skuAttrValueService.save(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuAttrValueEntity skuAttrValue){
		skuAttrValueService.updateById(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		skuAttrValueService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
