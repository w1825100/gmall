package com.atguigu.gmall.sms.mapper;

import com.atguigu.gmall.sms.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动场次
 * 
 * @author lgd
 * @email lgd@atguigu.com
 * @date 2021-01-18 18:59:31
 */
@Mapper
public interface SeckillSessionMapper extends BaseMapper<SeckillSessionEntity> {
	
}
