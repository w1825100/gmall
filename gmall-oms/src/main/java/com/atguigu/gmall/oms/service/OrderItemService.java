package com.atguigu.gmall.oms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.oms.entity.OrderItemEntity;

import java.util.Map;

/**
 * 订单项信息
 *
 * @author lgd
 * @email lgd@atguigu.com
 * @date 2021-01-18 21:10:29
 */
public interface OrderItemService extends IService<OrderItemEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}
