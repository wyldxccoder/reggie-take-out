package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import org.springframework.stereotype.Service;

import java.util.List;


public interface DishService extends IService<Dish> {
    /**
     * 同时保存菜品和口味两张表
     * @param dishDto
     */
    public void saveWithFavor(DishDto dishDto);

    /**
     * 同时查询菜品和口味两张表
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id);

    /**
     * 同时修改菜品和口味两张表
     * @param dishDto
     */
    void updateWithFlavor(DishDto dishDto);
    /**
     * 同时删除菜品和口味两张表
     */
    public void  removeWithFlavor(List<Long> ids);
}
