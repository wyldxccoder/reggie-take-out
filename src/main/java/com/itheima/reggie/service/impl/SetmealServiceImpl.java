package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.exception.CustomException;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐,同时需要保存套餐和菜品的关系 两张表
     * @param setmealDto
     */
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息
        this.save(setmealDto);

        //保存套餐和菜品关联信息
        //获取套餐id
        Long setmealId = setmealDto.getId();
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //setmealDto 没有套餐id
        List<SetmealDish> setmealDishList = setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishList);
    }

    /**
     * 同时查询套餐和菜品两张表
     * @param id
     * @return
     */
    @Override
    @Transactional
    public SetmealDto getByIdWithDish(Long id) {
        //查询套餐基础信息
        Setmeal setmeal= this.getById(id);

        SetmealDto setmealDto=new SetmealDto();
        //对象拷贝(将setmeal拷贝到setmealDto)
        BeanUtils.copyProperties(setmeal, setmealDto);
        //查询关联菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        //获取套餐id
        queryWrapper.eq(SetmealDish::getSetmealId, setmeal.getId());
        //将套餐id放入菜品中
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        //dishDto有基础套餐信息 添加菜品信息
        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }

    /**
     * 同时删除套餐和菜品两张表
     * @param
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long>ids) {
        //查询套餐状态 是否可删除
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count=this.count(queryWrapper);
        //如果不能,抛出异常
        if(count>0){
            throw new CustomException("套餐正在售卖,不能删除");
        }
        //如果能,先删除套餐表中的数据
        this.removeByIds(ids);
        //再删关联菜品的数据
        LambdaQueryWrapper<SetmealDish>lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lambdaQueryWrapper);

    }

    /**
     * 同时修改套餐和菜品关系表 两张表
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        //更新套餐基础信息
        this.updateById(setmealDto);
        //清除当前对应的口味数据
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //添加提交过来的数据
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //菜品口味 (DishDto类中没有封装菜品id 需要获取菜品id加入菜品口味中)
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        //保存菜品口味  菜品口味是数组 用saveBatch
        setmealDishService.saveBatch(setmealDishes);


    }
}
