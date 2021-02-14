package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.SkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * sku信息
 *
 * @author lgd
 * @email lgd@atguigu.com
 * @date 2021-01-18 18:14:15
 */
@Mapper
public interface SkuMapper extends BaseMapper<SkuEntity> {

    //演示sql注入,详见test
    List<SkuEntity> querySkuByIdNoStatement(String id);
}
