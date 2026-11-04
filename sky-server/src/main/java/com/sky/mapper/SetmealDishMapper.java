package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询多个套餐id
     * @return
     */
    List<Long> getSetmealDishIdsByDishIds(List<Long> dishIds);
}
