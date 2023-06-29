package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;


import java.util.List;


public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐,同时需要保存套餐和菜品的关系 两张表
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);
    /**
     * 同时查询套餐和菜品关系表两张表
     * @param id
     * @return
     */
    public SetmealDto getByIdWithDish(Long id);
    /**
     * 同时删除套餐和菜品关系表两张表
     */
    public void  removeWithDish(List<Long> ids);

    /**
     * 同时修改套餐和菜品关系表 两张表
     */
    void updateWithDish(SetmealDto setmealDto);

}
