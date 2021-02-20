package com.atguigu.gmall.common.aspect;


import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    /**
     * @desc 缓存前缀
     * @auth lgd
     * @Date 2021/2/8 1:07
     **/
    String prefix() default "";

    int timeout() default 5;

    int random() default 5;

    String lock() default "lock:";
}
