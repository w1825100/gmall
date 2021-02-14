package com.atguigu.gmall.wms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-01-28 22:17
 **/
public interface GmallWmsApi {
    @ApiOperation("根据skuid查询库存")
    @GetMapping("wms/waresku/sku/{id}")
     ResponseVo<List<WareSkuEntity>> queryWareSkuBySkuId(@PathVariable Long id);


}
