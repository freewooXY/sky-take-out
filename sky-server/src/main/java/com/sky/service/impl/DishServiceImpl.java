package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品以及口味
     * @param dishDTO
     */
    @Transactional // 涉及多个表的操作，需要进行事务管理
    public void createWithFlavor(DishDTO dishDTO) {
        // 创建dish对象
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 获取口味集合
        List<DishFlavor> flavorList = dishDTO.getFlavors();

        // 菜品表插入一条数据
        dishMapper.insert(dish);

        // 设置其中口味的dishId
        Long dishId = dish.getId();
        flavorList.forEach(flavor -> { flavor.setDishId(dishId); });

        // 口味表插入多条数据或者不插入数据
        if (flavorList != null && !flavorList.isEmpty()) {
            dishFlavorMapper.insertBatch(flavorList);
        }
    }
}
