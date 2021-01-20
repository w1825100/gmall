/**
  * Copyright 2021 bejson.com
  */
package com.atguigu.gmall.pms.vo;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Auto-generated: 2021-01-20 10:37:45
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class BaseAttrs extends SpuAttrValueEntity {


    private List<String> valueSelected;

    public void setValueSelected( List<String> valueSelected){
        if(CollectionUtils.isEmpty(valueSelected)){
            return ;
        }
            this.setAttrValue(StringUtils.join(valueSelected," "));
    }

}
