package com.itheima.reggie.exception;

/**
 * 自定义业务异常
 */
public class CustomException extends RuntimeException{
    public CustomException (String message){
        super(message);
    }
}
