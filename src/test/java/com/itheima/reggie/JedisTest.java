package com.itheima.reggie;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.util.Set;

/**
 * jedis操作redis
 */
public class JedisTest {
    @Test
    public void testRedis(){
        // 获取连接
        Jedis jedis =new Jedis("101.37.160.172",6379);
        jedis.auth("123456");

        //执行具体操作

      jedis.set("uername","xiaoming");
        jedis.hset("city","addr","bj");
        jedis.lpush("list","1","2","3");
        jedis.sadd("list2","1","2","3");
        jedis.zadd("list3",1.0,"b");
//        String hget = jedis.hget("city", "addr");
//        System.out.println(hget);


        //遍历所有存在 数据
        jedis.keys("*").forEach(System.out::println);
      //删除
        jedis.del("uername","city","list","list2","list3");
        //所有key
        jedis.keys("*");
        //是否存在
        jedis.exists("itcast");
        //数据类型
        jedis.type("city");




        //关闭连接
        jedis.close();


    }
}
