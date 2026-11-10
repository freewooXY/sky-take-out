package com.sky.service;

import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);


    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 退款成功，修改订单状态
     * @param outTradeNo
     */
    void refundSuccess(String outTradeNo);

    /**
     * 分页查询历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 查询订单详情
     * @param orders
     * @return
     */
    OrderVO orderDetail(Orders orders);

    /**
     * 取消订单
     * @param id
     */
    void cancel(Long id);

    /**
     * 订单支付的mock接口
     * @param ordersPaymentDTO
     */
    void paymentMock(OrdersPaymentDTO ordersPaymentDTO);

    /**
     * 再来一单
     * @param id
     */
    void repetition(Long id);

    /**
     * 订单退款Mock接口
     * @param id
     */
    void cancelMock(Long id);

    /**
     * 获取订单商户状态的数量
     * @return
     */
    OrderStatisticsVO getStatistics();

    /**
     * 商户接单
     * @param id
     */
    void confirm(Long id);

    /**
     * 商户拒单
     * @param ordersRejectionDTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     */
    void cancelByAdmin(OrdersCancelDTO ordersCancelDTO);

    /**
     * 指定订单号派送
     * @param id
     */
    void delivery(String id);
}
