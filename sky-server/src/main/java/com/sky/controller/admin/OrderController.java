package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.OrderStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminOrderController")
@Api(tags = "订单相关接口")
@Slf4j
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("查询订单")
    public Result<PageResult> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("查询订单：{}", ordersPageQueryDTO);

        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 查看订单状态统计数量
     * @return
     */
    @ApiOperation("查看订单状态统计数量")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics() {
        log.info("查看订单状态统计数量");

        OrderStatisticsVO orderStatisticsVO = orderService.getStatistics();

        return Result.success(orderStatisticsVO);
    }
}
