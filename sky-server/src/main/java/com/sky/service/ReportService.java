package com.sky.service;

import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {
    /**
     * 营业额数据统计
     * @param begin 开始时间
     * @param end 结束时间
     * @return 营业额数据
     */
    TurnoverReportVO getTurnover(LocalDate begin, LocalDate end);

    /**
     * 用户数据统计
     * @param begin 开始时间
     * @param end 结束时间
     * @return 用户数据
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单数据统计
     * @param begin 开始时间
     * @param end 结束时间
     * @return 订单数据
     */
    Object getOrderStatistics(LocalDate begin, LocalDate end);

    /**
     * 销量排名统计
      * @param begin 开始时间
      * @param end 结束时间
      * @return 销量排名数据
     */
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);
}
