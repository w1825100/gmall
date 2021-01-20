package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.dto.SkuSaleDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @program: gmall
 * @description: 营销管理远程接口
 * @author: 刘广典
 * @create: 2021-01-20 19:43
 **/

public interface GmallSmsApi {
    @PostMapping("sms/skubounds/save")
    ResponseVo saveSales(@RequestBody SkuSaleDto skuSaleDto);
}
