package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Result chageStatus(@PathVariable Integer status, Long id) {
        DishDTO dishDTO = new DishDTO();
        dishDTO.setStatus(status);
        dishDTO.setId(id);
        log.info("起售或者停售菜品：{}", dishDTO);

        dishService.changeStatus(dishDTO);

        return Result.success();
    }
}
