package com.itheima.reggie.exception;

import com.itheima.reggie.commen.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 全局异常处理器
 */
@Slf4j
@ResponseBody
@RestControllerAdvice (annotations = {RestController.class, Controller.class})//有这两个注解,可以使用异常处理器
public class GlobaExceptionHandler {
    @ExceptionHandler(Exception.class)//捕获全部异常
    public R <String>ex(Exception ex){
        log.info(ex.getMessage());
        if(ex.getMessage().contains("Duplicate entry")){ //
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }
        return R.error("未知错误");
}

    /**
     * 业务异常处理
     * @param ex
     * @return
     */
        @ExceptionHandler(CustomException.class)//捕获全部异常
        public R <String>ex(CustomException ex){
            log.info(ex.getMessage());
            return R.error(ex.getMessage());
        }



    }
