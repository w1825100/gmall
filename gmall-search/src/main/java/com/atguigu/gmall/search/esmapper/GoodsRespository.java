package com.atguigu.gmall.search.esmapper;

import com.atguigu.gmall.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @program: gmall
 * @description: es
 * @author: lgd
 * @create: 2021-01-28 21:05
 **/
public interface GoodsRespository  extends ElasticsearchRepository<Goods,Long> {

}
