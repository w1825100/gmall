package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.api.GmallSmsApi;
import com.atguigu.gmall.sms.vo.SkuSaleDto;
import io.seata.spring.annotation.GlobalTransactional;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.service.SpuService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * spu信息
 *
 * @author lgd
 * @email lgd@atguigu.com
 * @date 2021-01-18 18:14:15
 */
@Api(tags = "spu信息 管理")
@RestController
@RequestMapping("pms/spu")
public class SpuController {

    @Autowired
    private SpuService spuService;
    @Autowired
    RabbitTemplate rabbitTemplate;



    /**
     * feign
     */
    @PostMapping("json")
    @ApiOperation("分页查询")
    public ResponseVo<List<SpuEntity>> querySpuEntities(@RequestBody PageParamVo paramVo){
        PageResultVo pageResultVo = spuService.queryPage(paramVo);
        return ResponseVo.ok((List<SpuEntity>)pageResultVo.getList());
    }






    /**
     * 列表
     */
    @GetMapping("category/{id}")
    @ApiOperation("根据分类id查询spu信息")
    public ResponseVo<PageResultVo> querySpuByCidAndPage(@PathVariable("id") long id, PageParamVo paramVo){
        PageResultVo pageResultVo= spuService.querySpuByCidAndPage(id,paramVo);
        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySpuByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = spuService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id){
		SpuEntity spu = spuService.getById(id);

        return ResponseVo.ok(spu);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SpuVo spuvo){
		spuService.bigSave(spuvo);
        return ResponseVo.ok();
    }



    @Autowired
    GmallSmsClient smsClient;
    /**
     * 修改
     */
    @GlobalTransactional
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SpuEntity spu){
		spuService.updateById(spu);
        rabbitTemplate.convertAndSend("PMS_ITEM_EXCHANGE","item.update",spu.getId());
//        smsClient.saveSales(new SkuSaleDto());
        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		spuService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
