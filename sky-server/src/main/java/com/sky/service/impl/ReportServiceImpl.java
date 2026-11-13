package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获得统计营业额数据
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        // 当前集合用来存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = getDateList(begin, end);

        // 依次统计每一天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 查询date日期对应的已完成的订单营业额数据
            LocalDateTime dayBegin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("begin", dayBegin);
            map.put("end", dayEnd);
            map.put("status", Orders.COMPLETED);

            // select sum(amount) from orders where status = 5 and order_time between ? and ?
            Double turnover = orderMapper.sumByMap(map);
            turnoverList.add(turnover == null ? 0 : turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 统计用户数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        // 依次统计每一天的用户数量 和 新增用户
        List<Integer> newUserList = new ArrayList<>(); // 新增用户数量
        List<Integer> totalUserList = new ArrayList<>(); // 总用户数量
        for (LocalDate date : dateList) {
            LocalDateTime dayBegin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("end", dayEnd);

            // select count(id) from user where create_time < ? and create_time > ?
            // 统计全部用户
            totalUserList.add(userMapper.countByMap(map));

            // select count(id) from user where create_time < ?
            // 统计新增用户
            map.put("begin", dayBegin);
            newUserList.add(userMapper.countByMap(map));
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 统计订单数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        // 依次统计每日订单，有效订单，订单总数，有效订单总数，以及订单完成率
        List<Integer> orderCountList = new ArrayList<>(); // 每日订单
        List<Integer> validOrderCountList = new ArrayList<>(); // 有效订单
        for (LocalDate date : dateList) {
            LocalDateTime dayBegin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("begin", dayBegin);
            map.put("end", dayEnd);

            // 查询每天的订单数
            orderCountList.add(orderMapper.getCountByMap(map));
            // 查询每天的有效订单数
            map.put("status", Orders.COMPLETED);
            validOrderCountList.add(orderMapper.getCountByMap(map));
        }
        Integer orderCount = orderCountList.stream().reduce(0, Integer::sum);
        Integer validOrderCount = validOrderCountList.stream().reduce(0, Integer::sum);

        return OrderReportVO.builder()
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(orderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate((double) validOrderCount / (double) orderCount)
                .dateList(StringUtils.join(dateList, ","))
                .build();
    }

    /**
     * 记录从开始时间到结束时间的每一天
     * @param begin
     * @param end
     * @return
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        // 从begin开始到end的每一天
        dateList.add(begin);
        while ( (begin = begin.plusDays(1)).isBefore(end) ) {
            dateList.add(begin);
        }
        dateList.add(end);
        return dateList;
    }
}
