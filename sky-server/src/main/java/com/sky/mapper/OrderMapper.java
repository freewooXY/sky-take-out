package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 添加一个订单
     * @param orders
     */
    void insert(Orders orders);


    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 分页查询历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<OrderVO> pageQueryWithCondition(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 查询页面详情
     * @param orders
     * @return
     */
    OrderVO orderDetail(Orders orders);

    /**
     * 根据订单id获取订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 获取订单状态数量
     * @return
     */
    @Select("select count(1) from orders where status = #{status}")
    Integer getCountByStatus(Integer status);

    /**
     * 获取指定状态和小于时间的订单
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getByStatusTimeLessThan(Integer status, LocalDateTime time);
}
