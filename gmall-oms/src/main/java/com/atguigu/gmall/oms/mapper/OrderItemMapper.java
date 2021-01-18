package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author lgd
 * @email lgd@atguigu.com
 * @date 2021-01-18 21:10:29
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemEntity> {
	
}
