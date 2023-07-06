package com.itheima.reggie.commen;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用返回结果,服务端响应的数据最终都会封装成对象
 * @param <T>
 */
@Data
public class R<T> implements Serializable {

    private Integer code; // 编码：1成功，0和其它数字为失败 (Code: 1 for success, 0 or other numbers for failure)

    private String msg; // 错误信息 (Error message)

    private T data; // 数据 (Data)

    private Map map = new HashMap(); // 动态数据 (Dynamic data)

    // 创建并返回成功的响应对象，包含指定的数据
    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1; // 设置编码为1，表示成功
        return r;
    }

    // 创建并返回失败的响应对象，包含指定的错误信息
    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0; // 设置编码为0，表示失败
        return r;
    }

    // 向动态数据中添加键值对，并返回当前响应对象
    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }
}
