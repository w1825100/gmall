package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * spu属性值
 *
 * @author lgd
 * @email lgd@atguigu.com
 * @date 2021-01-18 18:14:15
 */
@Api(tags = "spu属性值 管理")
@RestController
@RequestMapping("pms/spuattrvalue")
public class SpuAttrValueController {

    @Autowired
    private SpuAttrValueService spuAttrValueService;


    @GetMapping("search/{cid}/{sid}")
    @ApiOperation("根据skuId和分类id查询spu属性")
    public ResponseVo<List<SpuAttrValueEntity>> querySpuAttrValueByCategoryIdAndSkuId(
            @PathVariable("cid") Long cid
            ,@PathVariable("sid") Long sid){

        List<SpuAttrValueEntity> spuAttrValueEntities= spuAttrValueService.querySpuAttrValueByCategoryIdAndSkuId(cid,sid);

        return ResponseVo.ok(spuAttrValueEntities);
    }











    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySpuAttrValueByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = spuAttrValueService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuAttrValueEntity> querySpuAttrValueById(@PathVariable("id") Long id){
		SpuAttrValueEntity spuAttrValue = spuAttrValueService.getById(id);

        return ResponseVo.ok(spuAttrValue);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SpuAttrValueEntity spuAttrValue){
		spuAttrValueService.save(spuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SpuAttrValueEntity spuAttrValue){
		spuAttrValueService.updateById(spuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		spuAttrValueService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
