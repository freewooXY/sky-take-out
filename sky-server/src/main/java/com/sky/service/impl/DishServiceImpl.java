package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
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

    @Autowired
    private SetmealDishMapper setmealDishMapper;

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

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        // 开始分页查询
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        // 获取分页
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 判断当前菜品是否能够删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            // 起售的菜品不能删除
            if (dish != null && dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 被套餐关联的不能删除
        List<Long> setmealIds = setmealDishMapper.getSetmealDishIdsByDishIds(ids);
        if (setmealIds != null && !setmealIds.isEmpty()) {
            throw new SetmealEnableFailedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品数据
        dishMapper.deleteBatchById(ids);
        // 删除口味数据
        dishFlavorMapper.deleteBatchByDishId(ids);
    }

    /**
     * 通过id查询菜品
     *
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        return dishMapper.getByIdWithFlavor(id);
    }

    /**
     * 修改菜品
     *
     * @param dishDTO
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        // 修改口味，先删除原有的数据，再插入新的数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        // 口味表要有口味
        if (dishDTO.getFlavors() != null && !dishDTO.getFlavors().isEmpty()) {
            for (DishFlavor dishFlavor : dishDTO.getFlavors()) {
                // 设置口味的关联菜品id
                dishFlavor.setDishId(dishDTO.getId());
            }
            dishFlavorMapper.insertBatch(dishDTO.getFlavors());
        }

        // 修改菜品信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
    }
}
