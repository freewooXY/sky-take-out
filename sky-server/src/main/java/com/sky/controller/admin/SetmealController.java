package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐相关接口
 */
@Api(tags = "套餐相关接口")
@Slf4j
@RequestMapping("/admin/setmeal")
@RestController
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 添加套餐接口
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("添加套餐接口")
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO) {
        log.info("添加套餐：{}",  setmealDTO);

        setmealService.addSetmeal(setmealDTO);

        return Result.success();
    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询套餐")
    public Result<PageResult> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("分页查询套餐：{}", setmealPageQueryDTO);

        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 批量删除套餐
     * @return
     */
    @ApiOperation("批量删除套餐")
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除套餐：{}", ids);

        setmealService.deleteBatch(ids);

        return Result.success();
    }

    /**
     * 套餐起售和停售
     * @return
     */
    @ApiOperation("套餐起售和停售")
    @PostMapping("/status/{status}")
    public Result changeStatus(@PathVariable Integer status, Long id) {
        log.info("套餐起售和停售：{} {}", status, id);

        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        setmeal.setId(id);

        setmealService.changeStatus(setmeal);

        return Result.success();
    }

    /**
     * 通过id查询套餐以及菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("通过id查询套餐以及菜品")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("通过id查询套餐以及菜品：{}", id);

        SetmealVO setmealVO = setmealService.getByIdWithDish(id);

        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @ApiOperation("修改套餐")
    @PutMapping
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐：{}", setmealDTO);

        setmealService.updateWithDish(setmealDTO);

        return Result.success();
    }
}
