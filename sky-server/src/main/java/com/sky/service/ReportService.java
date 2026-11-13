package com.sky.service;

import com.sky.vo.TurnoverReportVO;

import java.time.LocalDate;

public interface ReportService {

    /**
     * 获得统计营业额数据
     * @return
     */
    TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end);
}
