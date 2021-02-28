package com.atguigu.gmall.oms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import org.springframework.web.bind.annotation.*;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-27 09:02
 **/
public interface GmallOmsApi {
    @PostMapping("oms/order/save/{userId}")
     ResponseVo<OrderEntity> saveOrder(@RequestBody OrderSubmitVo submitVo, @PathVariable("userId")Long userId);

    @GetMapping("oms/order/query/{orderToken}")
     ResponseVo<OrderEntity> queryOrderByUserIdAndOrderToken(
            @PathVariable("orderToken")String orderToken,
            @RequestParam("userId")Long userId
    );
}
