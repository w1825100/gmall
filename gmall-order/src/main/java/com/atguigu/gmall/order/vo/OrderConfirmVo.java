package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.util.List;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-25 14:45
 **/
@Data
public class OrderConfirmVo {
    private List<UserAddressEntity> addresses;
    private List<OrderItemVo> orderItems;
    private Integer bounds;
    //幂等字段,防止重复提交
    private String orderToken;
}
