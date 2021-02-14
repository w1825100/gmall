package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-15 03:13
 **/
@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;

    @ResponseBody
    @GetMapping("{skuId}.html")
    public ResponseVo<ItemVo> toItem(@PathVariable Long skuId){
      ItemVo itemVo= itemService.loadData(skuId);
        return ResponseVo.ok(itemVo);
    }
}
