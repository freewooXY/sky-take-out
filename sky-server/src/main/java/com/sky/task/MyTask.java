package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 自定义定时任务类
 */
@Component
@Slf4j
public class MyTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单的方法
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void processTimeoutOrder() {
        log.info("定时处理超时订单：{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(15);
        List<Orders> timeoutOrders = orderMapper
                .getByStatusTimeLessThan(Orders.UN_PAID, time);

        if (timeoutOrders != null && !timeoutOrders.isEmpty()) {
            for (Orders orders : timeoutOrders) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("订单超时自动取消");
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder() {
        log.info("处理派送中的订单：{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().minusHours(1);

        List<Orders> deliveryOrders = orderMapper
                .getByStatusTimeLessThan(Orders.DELIVERY_IN_PROGRESS, time);

        if (deliveryOrders != null && !deliveryOrders.isEmpty()) {
            for (Orders orders : deliveryOrders) {
                orders.setStatus(Orders.CANCELLED);
                orderMapper.update(orders);
            }
        }
    }
}
