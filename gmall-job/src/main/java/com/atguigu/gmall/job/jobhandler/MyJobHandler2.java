package com.atguigu.gmall.job.jobhandler;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-25 10:15
 **/


public class MyJobHandler2 extends IJobHandler {


    @Override
    public ReturnT<String> execute(String param) throws Exception {
        System.out.println("MyJobHandler2触发了");

        return ReturnT.SUCCESS;
    }
}
