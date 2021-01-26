package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * spu信息介绍
 *
 * @author lgd
 * @email lgd@atguigu.com
 * @date 2021-01-18 18:14:15
 */
@Mapper
public interface SpuDescMapper extends BaseMapper<SpuDescEntity> {

    @Select("select * from pms_spu_desc where  spu_id = #{id}")
    SpuDescEntity search(long id);
}
