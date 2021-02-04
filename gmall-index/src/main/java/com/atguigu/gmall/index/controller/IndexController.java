package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @program: gmall
 * @description: 首页
 * @author: lgd
 * @create: 2021-02-05 02:12
 **/
@Controller
public class IndexController {
    @Autowired
    IndexService indexService;

    @GetMapping
    public String toIndex(Model model){
    List<CategoryEntity> categories=indexService.queryLV1Categories();
        model.addAttribute("categories",categories);
        return "index";
    }
    @ResponseBody
    @GetMapping("index/cates/{pid}")
    public ResponseVo<List<CategoryEntity>> getSubCategories(@PathVariable Long pid){
        ResponseVo<List<CategoryEntity>>res=indexService.getSubCategories(pid);
        return res;
    }

}
