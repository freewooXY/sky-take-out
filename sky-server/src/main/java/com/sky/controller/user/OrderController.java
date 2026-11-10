package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.properties.MockProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/user/order")
@RestController("userOrderController")
@Api(tags = "订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    private MockProperties mockProperties;

    @Autowired
    private OrderService orderService;

    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @ApiOperation("提交订单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("提交订单：{}", ordersSubmitDTO);

        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);

        return Result.success(orderSubmitVO);
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);

        if (mockProperties.isPayMock()) {
            return paymentMock(ordersPaymentDTO);
        }

        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);

        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 订单支付模拟接口
     * @return
     */
    public Result paymentMock(@RequestBody OrdersPaymentDTO ordersPaymentDTO) {
        log.info("跳过微信支付，完成订单支付：{}", ordersPaymentDTO);

        orderService.paymentMock(ordersPaymentDTO);

        return Result.success();
    }

    /**
     * 查看历史订单
     * @return
     */
    @ApiOperation("查看历史订单")
    @GetMapping("/historyOrders")
    public Result<PageResult> history(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("查看历史订单：{}", ordersPageQueryDTO);

        PageResult result = orderService.pageQuery(ordersPageQueryDTO);

        return Result.success(result);
    }

    /**
     * 查看订单详情
     * @param id
     * @return
     */
    @ApiOperation("查看订单详情")
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> orderDetail(@PathVariable Long id) {
        log.info("查看订单详情：{}", id);

        Orders orders = new Orders();
        orders.setId(id);

        OrderVO orderVO = orderService.orderDetail(orders);

        return Result.success(orderVO);
    }

    /**
     * 取消订单
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id) {
        log.info("取消订单：{}", id);

        if (mockProperties.isPayMock()) {
            return cancelMock(id);
        }

        orderService.cancel(id);

        return Result.success();
    }

    public Result cancelMock(Long id) {
        log.info("跳过微信退款，直接退款成功：{}", id);

        orderService.cancelMock(id);

        return Result.success();
    }

    /**
     * 再来一单
     * @return
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id) {
        log.info("再来一单：{}", id);

        orderService.repetition(id);

        return Result.success();
    }
}
