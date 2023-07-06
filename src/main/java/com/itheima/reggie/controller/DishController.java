package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.commen.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {

        //修改菜品需要清理redis缓存  保证数据的正确性
        // Set keys = redisTemplate.keys("dish_*");  //清理所有key
        //清理单个key
        String keys="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(keys);

        dishService.saveWithFavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        //排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //分页查询
        dishService.page(pageInfo, queryWrapper);//没有分类名称 dish里面只有分类id没有分类名称

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records"); //不拷贝records records是数组 实体类对象

        List<Dish> records = pageInfo.getRecords();

        //处理records里面添加分类名称
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            //获取分类名称
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).collect(Collectors.toList());

        //将分类名称加入records
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 修改菜品 显示原来菜品
     */
    @GetMapping("/{id}")
    public R<DishDto> update(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {

        //修改菜品需要清理redis缓存  保证数据的正确性
        // Set keys = redisTemplate.keys("dish_*");  //清理所有key
        //清理单个key
        String keys="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(keys);


        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    /**
     * 根据id修改菜品的状态status(停售和起售)
     * <p>
     * 0停售，1起售。
     *
     * @param status
     * @param
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatusById(@PathVariable Integer status, Long[] ids) {
        // 增加日志验证是否接收到前端参数。
        log.info("根据id修改菜品的状态:{},id为：{}", status, ids);

        // 遍历每个id,修改id为ids数组中的数据的菜品状态status为前端页面提交的status。
        for (Long id : ids) {
            //根据id得到每个dish菜品。
            Dish dish = dishService.getById(id);

            //修改菜品销售状态需要清理redis缓存  保证数据的正确性
            // Set keys = redisTemplate.keys("dish_*");  //清理所有key
            //清理单个key
            String keys="dish_"+dish.getCategoryId()+"_1";
            redisTemplate.delete(keys);


            //设置菜品的状态
            dish.setStatus(status);
            //更新菜品
            dishService.updateById(dish);

        }
        return R.success("修改菜品状态成功");
    }

    /**
     * 根据id删除菜品
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {

        dishService.removeWithFlavor(ids);
        return R.success("删除菜品成功");
    }

    /**
     * 根据id查询菜品  套餐里面显示
     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish) {
//        //条件构造器
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        //只能查询起售状态的菜品
//        queryWrapper.eq(Dish::getStatus, 1);
//        //添加过滤条件 套餐id存在
//        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
//        //添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(queryWrapper);
//        return R.success(list);
//
//    }

    /**
     * 移动端菜品展示
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {

        List<DishDto> dishDtoList=null;
        //动态构造key
        String key="dish_"+dish.getCategoryId()+"_1";
        //先从redis中获取缓存数据
        dishDtoList  = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if(dishDtoList!=null){
            //如果存在,直接返回,不需要在查数据库
            return R.success(dishDtoList);
        }
        //如果不存在 查数据库,存入redis缓存中

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //只能查询起售状态的菜品
        queryWrapper.eq(Dish::getStatus, 1);
        //添加过滤条件 套餐id存在
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

         dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            //获取分类名称
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            //追加菜品口味
            //当前菜品的id
            Long dishid = item.getId();
            //sql:select*from dish_flavor where dish_id=?
            LambdaQueryWrapper<DishFlavor>lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishid);
            List<DishFlavor> dishFlavors = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());
        //如果不存在 查数据库,存入redis缓存中
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);

    }
}
