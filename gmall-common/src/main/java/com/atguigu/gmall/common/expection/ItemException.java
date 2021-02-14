package com.atguigu.gmall.common.expection;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-15 03:33
 **/
public class ItemException extends RuntimeException {

    public ItemException() {
        super();
    }

    public ItemException(String message) {
        super(message);
    }
}
