package com.atguigu.gmall.index.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-05 02:15
 **/
@Service
public class IndexService {
    @Autowired
    GmallPmsClient gmallPmsClient;


    public List<CategoryEntity> queryLV1Categories() {
        ResponseVo<List<CategoryEntity>> categoryEntityResponseVo = gmallPmsClient.queryCategoryListByPid(0l);
        return categoryEntityResponseVo.getData();
    }

    public ResponseVo<List<CategoryEntity>> getSubCategories(Long pid) {
        ResponseVo<List<CategoryEntity>> categoryEntityResponseVo = gmallPmsClient.getSubsCategories(pid);

        return categoryEntityResponseVo;
    }
}
