package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 *
 * @author lgd
 * @email lgd@atguigu.com
 * @date 2021-01-18 20:41:42
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

}
