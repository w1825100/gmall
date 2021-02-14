package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleDto;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @program: gmall
 * @description: 营销管理远程接口
 * @author: lgd
 * @create: 2021-01-20 19:43
 **/

public interface GmallSmsApi {
    @PostMapping("sms/skubounds/save")
    @ApiOperation("保存营销信息")
    ResponseVo saveSales(@RequestBody SkuSaleDto skuSaleDto);

    @GetMapping("sms/skubounds/sku/{id}")
    @ApiOperation("根据skuId查询营销信息")
   ResponseVo<List<ItemSaleVo>> querySalesBySkuId(@PathVariable Long id);

}
