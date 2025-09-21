package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    /**
     * 根据套餐id查询套餐菜品关系
     *
     * @param ids
     */
    List<Long> getSetmealId(List<Long> ids);
}
