package com.atguigu.gmall.auth.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-19 15:33
 **/
public interface AuthService {

    void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response);
}
