package com.atguigu.gmall.cart.api;

import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-24 21:15
 **/
public interface GmallCartApi {
    @GetMapping("delete/{userId}")
    @ApiOperation("根据用户id删除mysql购物车")
    ResponseVo deleteByUserId(@PathVariable String userId);


    @ApiOperation("查询购物车选中状态")
    @GetMapping("user/{userId}")
    @ResponseBody
     ResponseVo<List<Cart>> queryCheckedCartsByUserId(@PathVariable("userId")Long userId);

}
