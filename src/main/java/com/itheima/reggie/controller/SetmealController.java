package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.commen.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService service;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//清除所有缓存    使用spring cache缓存
    //   @CacheEvict(value = "setmealCache", key = "#setmealDto.categoryId+'_'+#setmealDto.status" ) //清除单个的key
    public R<String> save(@RequestBody SetmealDto setmealDto) {

        //添加套餐 需要清理redis缓存  保证数据的正确性
        // Set keys = redisTemplate.keys("setmeal_*");  //清理所有key
        //清除单个的key
//        String keys="setmeal_"+setmealDto.getCategoryId()+"_1";
//        redisTemplate.delete(keys);

        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //过滤条件
        queryWrapper.like(name != null, Setmeal::getName, name);
        //排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //分页查询
        setmealService.page(pageInfo, queryWrapper);//没有分类名称 dish里面只有分类id没有分类名称

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records"); //不拷贝records records是数组 实体类对象

        List<Setmeal> records = pageInfo.getRecords();

        //处理records里面添加分类名称
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();

            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            //获取分类名称
            String categoryName = category.getName();
            setmealDto.setCategoryName(categoryName);
            return setmealDto;
        }).collect(Collectors.toList());

        //将分类名称加入records
        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }

    /**
     * 删除套餐
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {

        setmealService.removeWithDish(ids);
        return R.success("删除套餐成功");
    }

    /**
     * 根据id(批量)停售/启售套餐信息
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
  @CacheEvict(value = "setmealCache",allEntries = true)//清除所有缓存  使用spring cache缓存
  //@CacheEvict(value = "setmealCache", key = "#setmeal.categoryId+'_'+ #setmeal.status" )//清除单个的key
    public R<String> updateMulStatus(@PathVariable Integer status, Long[] ids) {
        List<Long> list = Arrays.asList(ids);
        for (Long id : ids) {
            Setmeal setmeal = setmealService.getById(id);
        }

//        for (Long id : ids) {
//            Setmeal setmeal = setmealService.getById(id);
//            //修改套餐销售状态需要清理redis缓存  保证数据的正确性
//            // Set keys = redisTemplate.keys("dish_*");  //清理所有key
//            //清理单个key
//            String keys="dish_"+setmeal.getCategoryId()+"_1";
//            redisTemplate.delete(keys);
//
//        }

        //构造条件构造器
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        //添加过滤条件
        updateWrapper.set(Setmeal::getStatus, status).in(Setmeal::getId, list);
        setmealService.update(updateWrapper);

        return R.success("套餐信息修改成功");
    }

    /**
     * 修改套餐 显示原来套餐
     */
    @GetMapping("/{id}")
    public R<SetmealDto> update(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    /**
     * 修改套餐
     */
    @PutMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//清除所有缓存    使用spring cache缓存
    //   @CacheEvict(value = "setmealCache", key = "#setmealDto.categoryId+'_'+#setmealDto.status")//清除单个的key
    public R<String> update(@RequestBody SetmealDto setmealDto) {

        //修改套餐 需要清理redis缓存  保证数据的正确性
        // Set keys = redisTemplate.keys("setmeal_*");  //清理所有key
        //清理单个的key
//        String keys="setmeal_"+setmealDto.getCategoryId()+"_1";
//        redisTemplate.delete(keys);


        setmealService.updateWithDish(setmealDto);
        return R.success("修改菜品成功");
    }

    /**
     * 根据条件查询套餐数据 移动端展示
     *
     * @param setmeal
     * @return
     */

//     Cacheable:在方法执行前spring先查看缓存中是否有数据，如果有数据，则直接返回缓存数据;若没有数据，调用方法并将方法返回值放到缓存中
//     若数据库没有找到数据，以null为key
//     condition：缓存条件，满足条件才缓存
//@Cacheable(value = "userCache", key = "#id", unless = "#result == null")

    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_ '+ #setmeal.status") //  使用spring cache缓存
    public R<List<Setmeal>> list(Setmeal setmeal) {

//       List<Setmeal> setmealList=null;
//       //动态构造key
//       String key="setmeal_"+setmeal.getCategoryId()+"_"+setmeal.getStatus();
//       //先从redis中获取缓存数据
//       setmealList  = (List<Setmeal>) redisTemplate.opsForValue().get(key);
//       if(setmealList!=null){
//           //如果存在,直接返回,不需要在查数据库
//           return R.success(setmealList);
//       }
        //如果不存在 查数据库,存入redis缓存中
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        //如果不存在 查数据库,存入redis缓存中
//       redisTemplate.opsForValue().set(key,setmealList,60, TimeUnit.MINUTES);

        return R.success(list);

    }
}


