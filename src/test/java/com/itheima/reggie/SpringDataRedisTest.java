package com.itheima.reggie;

import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.test.context.junit4.SpringRunner;


import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@SpringBootTest
@RunWith(SpringRunner.class)
public class SpringDataRedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 操作String类型数据
     */
    @Test
    public void testString() {
       //存值
        redisTemplate.opsForValue().set("city", "bj");

        //取值
        redisTemplate.opsForValue().get("city");

        //存值 有生命周期
        redisTemplate.opsForValue().set("key1", "value", 10l, TimeUnit.SECONDS);


        //存值 不重复可存
        redisTemplate.opsForValue().setIfAbsent("city", "nj");
    }

    /**
     * 操作Hash类型数据
     */
    @Test
    public void testHash() {


        //存值
        //redisTemplate.opsForHash().putAll();
        redisTemplate.opsForHash().put("002", "name", "xiaoming");
        redisTemplate.opsForHash().put("002", "age", "20");
        redisTemplate.opsForHash().put("002", "address", "bj");

        //取值
        String age = (String) redisTemplate.opsForHash().get("002", "age");
        System.out.println(age);

        //获得hash结构中的所有字段 file
        redisTemplate.opsForHash().keys("002").forEach(System.out::println);


        //获得hash结构中的所有值
        redisTemplate.opsForHash().values("002").forEach(System.out::println);

    }

    /**
     * 操作List类型的数据  有序 可重复
     */
    @Test
    public void testList() {

        //存值
        redisTemplate.opsForList().leftPush("mylist", "a");

        redisTemplate.opsForList().leftPushAll("mylist", "b", "c");

        //取值
        redisTemplate.opsForList().range("mylist", 0, -1).forEach(System.out::println);

        //获得列表长度 llen
        int size = redisTemplate.opsForList().size("mylist").intValue();


//        for (int i = 0; i < size; i++) {
//            //出队列
//            String element = (String)redisTemplate.opsForList().rightPop("mylist");
//            System.out.println(element);
//        }
    }

    /**
     * 操作Set类型的数据  无序 不重复
     */
    @Test
    public void testSet() {


        //存值

        redisTemplate.opsForSet().add("myset", "a", "b", "c", "a");

        //取值
        redisTemplate.opsForSet().members("myset").forEach(System.out::println);

//        //删除成员
//        redisTemplate.opsForSet().remove("myset", "a", "b");
//
//        //取值
//        redisTemplate.opsForSet().members("myset").forEach(System.out::println);


    }

    /**
     * 操作ZSet类型的数据  有序  不重复
     */
    @Test
    public void testZset() {


        //存值

        redisTemplate.opsForZSet().add("myZset", "a", 10.0);
        redisTemplate.opsForZSet().add("myZset", "b", 11.0);
        redisTemplate.opsForZSet().add("myZset", "c", 12.0);
        redisTemplate.opsForZSet().add("myZset", "a", 13.0);

        //取值
        redisTemplate.opsForZSet().range("myZset", 0, -1).forEach(System.out::println);

        //修改分数
        redisTemplate.opsForZSet().incrementScore("myZset", "b", 20.0);

        //取值
        redisTemplate.opsForZSet().range("myZset", 0, -1).forEach(System.out::println);

        //删除成员
        redisTemplate.opsForZSet().remove("myZset", "a", "b");

        //取值
         redisTemplate.opsForZSet().range("myZset", 0, -1).forEach(System.out::println);

        }


    /**
     * 通用操作，针对不同的数据类型都可以操作
     */
    @Test
    public void testCommon() {
        //获取Redis中所有的key
         redisTemplate.keys("*").forEach(System.out::println);



        //判断某个key是否存在
        System.out.println(redisTemplate.hasKey("itcast"));


//        //删除指定key
//        redisTemplate.delete("myZset");
//
        //获取指定key对应的value的数据类型
        System.out.println(redisTemplate.type("myset"));


    }
}
