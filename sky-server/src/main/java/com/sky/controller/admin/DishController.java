package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品相关接口
 */
@Api(tags = "菜品相关接口")
@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result create(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);

        dishService.createWithFlavor(dishDTO);

        // 清理缓存数据
        cleanCache("dish_" +  dishDTO.getCategoryId());

        return Result.success();
    }

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询菜品")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询菜品：{}",  dishPageQueryDTO);

        PageResult dishVoList = dishService.pageQuery(dishPageQueryDTO);

        return Result.success(dishVoList);
    }

    /**
     * 批量删除菜品
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品：{}", ids);

        dishService.deleteBatch(ids);

        /* 为了避免通过查询数据库获取哪一些分类收到了影响
         直接使用比较大粒度的清除缓存操作，清除全部缓存
         */
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 通过id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("通过id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("通过id查询菜品：{}", id);

        DishVO dishVO = dishService.getByIdWithFlavor(id);

        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);

        dishService.updateWithFlavor(dishDTO);

        /*
            修改菜品可能没有修改分类，也有可能修改了分类。
            有可能影响一个或者两个分类。
            在这里不将代码复杂化，直接使用粗粒度操作，删除所有的缓存数据
         */
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 起售或者停售菜品
     * @param id
     * @param status
     * @return
     */
    @ApiOperation("起售或者停售菜品")
    @PostMapping("status/{status}")
    public Result changeStatus(@PathVariable Integer status, Long id) {
        DishDTO dishDTO = new DishDTO();
        dishDTO.setStatus(status);
        dishDTO.setId(id);
        log.info("起售或者停售菜品：{}", dishDTO);

        dishService.changeStatus(dishDTO);

        /*
            同样的理由进行一个粗粒度操作
         */
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 根据分类查询菜品
     */
    @ApiOperation("根据分类查询菜品")
    @GetMapping("/list")
    public Result<List<Dish>> getByCategoryId(Long categoryId) {
        log.info("根据分类查询菜品：{}", categoryId);

        List<Dish> dishList = dishService.getByCategoryId(categoryId);

        return Result.success(dishList);
    }

    private void cleanCache(String pattern) {
        redisTemplate.delete(redisTemplate.keys(pattern));
    }
}
