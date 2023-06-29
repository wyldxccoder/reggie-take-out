package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.exception.CustomException;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品,同时保存响应的口味
     *
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFavor(DishDto dishDto) {
        //保存菜品的基础信息
        this.save(dishDto);
        //获取菜品id
        Long dishId = dishDto.getId();
        //菜品口味 (DishDto类中没有封装菜品id 需要获取菜品id加入菜品口味中)
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存菜品口味  菜品口味是数组 用saveBatch
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    @Override
    @Transactional
    public DishDto getByIdWithFlavor(Long id) {
        //显示菜品基础信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        //对象拷贝(将dish拷贝到idshdto)
        BeanUtils.copyProperties(dish, dishDto);
        //查询菜品口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        //获取菜品id
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        //将菜品id放入菜品口味中
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        //dishDto有基础菜品信息 添加菜品口味信息
        dishDto.setFlavors(list);

        return dishDto;

    }

    /**
     * 修改菜品
     *
     * @param dishDto
     */

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新菜品基础信息
        this.updateById(dishDto);
        //清除当前对应的口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //添加提交过来的数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        //菜品口味 (DishDto类中没有封装菜品id 需要获取菜品id加入菜品口味中)
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        //保存菜品口味  菜品口味是数组 用saveBatch
        dishFlavorService.saveBatch(flavors);


    }

    @Override
    @Transactional
    public void removeWithFlavor(List<Long> ids) {
        //查询套餐状态 是否可删除
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,1);
        int count=this.count(queryWrapper);
        //如果不能,抛出异常
        if(count>0){
            throw new CustomException("菜品正在售卖,不能删除");
        }
        //如果能,先删除套餐表中的数据
        this.removeByIds(ids);
        //再删关联菜品的数据
        LambdaQueryWrapper<DishFlavor>lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(lambdaQueryWrapper);


    }
}
