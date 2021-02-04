package com.atguigu.gmall.search.controller;


import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-01-29 23:06
 **/
@Controller
@RequestMapping("search")
public class SearchController {

    @Autowired
    SearchService searchService;

    @GetMapping
    public String search(SearchParamVo searchParamVo, Model model){

        SearchResponseVo searchRes= searchService.search(searchParamVo);
        model.addAttribute("response",searchRes);
        model.addAttribute("searchParam",searchParamVo);
        return "search";
    }
    @GetMapping("1")
    @ResponseBody
    public SearchResponseVo search1(SearchParamVo searchParamVo){
        System.out.println(searchParamVo);
        SearchResponseVo searchRes= searchService.search(searchParamVo);
        System.out.println(searchRes);
        return searchRes;
    }
}
