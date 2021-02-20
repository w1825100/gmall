package com.atguigu.gmall.common.expection;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.baomidou.mybatisplus.extension.api.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-02-17 02:21
 **/
@RestControllerAdvice
@Slf4j
public class GlobalException {
    @ExceptionHandler(Exception.class)
    public ResponseVo error(Exception e){
        log.error(ExceptionUtils.getStackTrace(e));
        return ResponseVo.fail("未知错误");
    }

    @ExceptionHandler(GmallException.class)
    @ResponseBody
    public ResponseVo error(GmallException e){
        log.error(ExceptionUtils.getStackTrace(e));
        return ResponseVo.fail(e.getMessage());
    }
}
