package com.atguigu.gmall.ums.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.ums.entity.UserEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-19 15:12
 **/
public interface GmallUmsApi {
    @GetMapping("ums/user/query")
    @ApiOperation("查询用户/单点登录")
    ResponseVo<UserEntity> queryUser(@RequestParam("loginName") String loginName, @RequestParam("password") String password);
}
