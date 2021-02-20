package com.atguigu.gmall.common.expection;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-17 02:23
 **/
public class GmallException  extends RuntimeException {
    public GmallException() {
        super();
    }

    public GmallException(String message) {
        super(message);
    }
}
