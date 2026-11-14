package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

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
        Integer orderCount = orderCountList.stream()
                .reduce(0, Integer::sum); // 统计订单总数
        Integer validOrderCount = validOrderCountList.stream()
                .reduce(0, Integer::sum); // 统计有效订单数

        return OrderReportVO.builder()
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(orderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCount == 0 ? 0 : (double) validOrderCount / (double) orderCount)
                .dateList(StringUtils.join(dateList, ","))
                .build();
    }

    /**
     * 统计时间段内销量前十的菜品
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO topTen(LocalDate begin, LocalDate end) {
        Map map = new HashMap();
        map.put("begin", LocalDateTime.of(begin, LocalTime.MIN));
        map.put("end", LocalDateTime.of(end, LocalTime.MAX));
        // 查询销量排名前十的菜品
        List<GoodsSalesDTO> list = orderMapper.getSalesTop10ByMap(map);

        // 获取菜品名称和销量
        List nameList = list.stream()
                .map( GoodsSalesDTO::getName ).collect(Collectors.toList());
        List numberList = list.stream()
                .map( GoodsSalesDTO::getNumber ).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    /**
     * 导出营业数据报表
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 查询数据库，获取营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30); // 起始日期为30天以前
        LocalDate dateEnd = LocalDate.now().minusDays(1); // 从昨天开始统计
        BusinessDataVO businessData = workspaceService.getBusinessData(
                LocalDateTime.of(dateBegin, LocalTime.MIN),
                LocalDateTime.of(dateEnd, LocalTime.MAX)
        ); // 查询概览数据

        // 将数据写入excel文件之中
        InputStream is = this.getClass().getClassLoader()
                .getResourceAsStream("template/运营数据报表模板.xlsx"); // 获取输入流对象
        try {
            // 基于模板文件创建excel文件
            XSSFWorkbook excel = new XSSFWorkbook(is);

            // 获取表单 填写数据
            XSSFSheet sheet = excel.getSheetAt(0);

            sheet.getRow(1)
                    .getCell(1)
                    .setCellValue("时间：" + dateBegin + "至" + dateEnd); // 填写时间

            sheet.getRow(3)
                    .getCell(2)
                    .setCellValue(businessData.getTurnover()); // 填写营业额
            sheet.getRow(3)
                    .getCell(4)
                    .setCellValue(businessData.getOrderCompletionRate()); // 填写订单完成率
            sheet.getRow(3)
                    .getCell(6)
                    .setCellValue(businessData.getNewUsers()); // 填写新增用户

            sheet.getRow(4)
                    .getCell(2)
                    .setCellValue(businessData.getValidOrderCount()); // 填写有效订单数
            sheet.getRow(4)
                    .getCell(4)
                    .setCellValue(businessData.getUnitPrice()); // 填写客单价

            for (int i = 0; i < 30; i++) {
                // 填充30次明细数据
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO data = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX)
                );// 查询某一天的营业数据

                XSSFRow row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString()); // 日期
                row.getCell(2).setCellValue(data.getTurnover()); // 营业额
                row.getCell(3).setCellValue(data.getValidOrderCount()); // 有效订单数
                row.getCell(4).setCellValue(data.getOrderCompletionRate()); // 订单完成率
                row.getCell(5).setCellValue(data.getUnitPrice()); // 客单价
                row.getCell(6).setCellValue(data.getNewUsers()); // 新增用户
            }

            // 通过输出流将excel返回给客户端
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            outputStream.close();
            excel.close();
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
