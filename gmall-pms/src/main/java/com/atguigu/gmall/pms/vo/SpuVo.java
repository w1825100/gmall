/**
  * Copyright 2021 bejson.com
  */
package com.atguigu.gmall.pms.vo;
import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

/**
 * Auto-generated: 2021-01-20 10:37:45
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class SpuVo extends SpuEntity {

    private List<String> spuImages;
    private List<BaseAttrs> baseAttrs;
    private List<Skus> skus;

}
