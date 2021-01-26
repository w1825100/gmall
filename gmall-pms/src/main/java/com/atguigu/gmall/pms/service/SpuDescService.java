package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SpuDescEntity;

import java.util.List;
import java.util.Map;

/**
 * spu信息介绍
 *
 * @author lgd
 * @email lgd@atguigu.com
 * @date 2021-01-18 18:14:15
 */
public interface SpuDescService extends IService<SpuDescEntity> {


    void saveSpuDesc(Long spuId, List<String> spuImages);
    PageResultVo queryPage(PageParamVo paramVo);

}

