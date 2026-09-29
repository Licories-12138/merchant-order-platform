package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
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

public class ReportServiceImpl implements ReportService{
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额数据统计
     * @param begin 开始时间
     * @param end 结束时间
     * @return 营业额数据
     */
    @Override
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        // 循环结束后，dateList 里就装满了从 begin 到 end 之间的每一天（包含首尾两端）
        while (!begin.equals(end)){
            begin = begin.plusDays(1);//日期计算，获得指定日期后1天的日期
            dateList.add(begin);
        }
        // 第二步：循环查询每天的营业额（turnoverList）
        List<Double> turnoverList = new ArrayList<>();
        // 第三步：将 dateList 和 turnoverList 拼接成字符串，用逗号分隔
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();
            map.put("status", Orders.COMPLETED);
            map.put("begin",beginTime);
            map.put("end", endTime);
            Double turnover = orderMapper.sumByMap(map); // 查询每天的营业额
            turnover = turnover == null ? 0.0 : turnover; // 如果 turnover 为 null，则赋值为 0.0
            turnoverList.add(turnover); // 将营业额添加到 turnoverList 中
        }

        //数据封装
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    /**
     * 用户数据统计
     * @param begin 开始时间
     * @param end 结束时间
     * @return 用户数据
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        // 循环结束后，dateList 里就装满了从 begin 到 end 之间的每一天（包含首尾两端）
        while (!begin.equals(end)){
            begin = begin.plusDays(1);//日期计算，获得指定日期后1天的日期
            dateList.add(begin);
        }
        // 第二步：循环查询每天的新增用户数和总用户数
        List<Integer> newUserList = new ArrayList<>(); //新增用户数
        List<Integer> totalUserList = new ArrayList<>(); //总用户数

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //新增用户数量 select count(id) from user where create_time > ? and create_time < ?
            Integer newUser = getUserCount(beginTime, endTime);
            //总用户数量 select count(id) from user where  create_time < ?
            Integer totalUser = getUserCount(null, endTime);
            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }

    /**
     * 根据时间区间统计用户数量
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 用户数量
     */
    private Integer getUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        Map<String, Object> map = new HashMap<>();
        map.put("begin",beginTime);
        map.put("end", endTime);
        return userMapper.countByMap(map);
    }

    /**
     * 订单数据统计
     * @param begin 开始时间
     * @param end 结束时间
     * @return 订单数据
     */
    @Override
    public Object getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        // 循环结束后，dateList 里就装满了从 begin 到 end 之间的每一天（包含首尾两端）
        while (!begin.equals(end)){
            begin = begin.plusDays(1);//日期计算，获得指定日期后1天的日期
            dateList.add(begin);
        }
        //每天订单总数集合
        List<Integer> orderCountList = new ArrayList<>();
        //每天有效订单数集合
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //查询每天的总订单数 select count(id) from orders where order_time > ? and order_time < ?
            Integer orderCount = getOrderCount(beginTime, endTime, null);

            //查询每天的有效订单数 select count(id) from orders where order_time > ? and order_time < ? and status = ?
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        //时间区间内的总订单数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).orElse(0);
        //时间区间内的总有效订单数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).orElse(0);
        //订单完成率
        double orderCompletionRate = 0.0;
        if(totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 根据时间区间统计指定状态的订单数量
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param status 订单状态
     * @return 订单数量
     */
    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("begin",beginTime);
        map.put("end", endTime);
        return orderMapper.countByMap(map);
    }

    /**
     * 销量排名数据统计
     * @param begin 开始时间
     * @param end 结束时间
     * @return 销量排名数据
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        // 确保 SQL 查询时，包含开始当天的所有订单，也包含结束当天的所有订单，不遗漏任何毫秒级数据
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        // 查询数据库
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime, endTime);

        // 数据提取与字符串拼接
        String nameList = StringUtils.join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList()),",");
        String numberList = StringUtils.join(goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList()),",");

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出30天的业务数据
     * @param response 响应对象
     */
    @SuppressWarnings("VulnerableCodeUsages")
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        // 1. 查询概览数据
        BusinessDataVO businessData = workspaceService.getBusinessData(
                LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX));

        // 2. 获取输入流
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream("template/运营数据报表模板.xlsx");

        // ✅ 加上这段判空逻辑
        if (inputStream == null) {
            log.error("Excel模板文件未找到: template/运营数据报表模板.xlsx");
            throw new RuntimeException("系统模板缺失，请联系管理员");
        }

        try (XSSFWorkbook excel = new XSSFWorkbook(inputStream);
             ServletOutputStream out = response.getOutputStream()) {

            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue(begin + "至" + end);

            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            // 2. 循环30天（老老实实查库，保证100%不报错）
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);

                BusinessDataVO dailyData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));

                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(dailyData.getTurnover());
                row.getCell(3).setCellValue(dailyData.getValidOrderCount());
                row.getCell(4).setCellValue(dailyData.getOrderCompletionRate());
                row.getCell(5).setCellValue(dailyData.getUnitPrice());
                row.getCell(6).setCellValue(dailyData.getNewUsers());
            }

            // 3. 写入输出流
            excel.write(out);
            out.flush();

        } catch (Exception e) {
            log.error("导出运营数据报表失败", e);
            throw new RuntimeException("报表下载失败");
        }
    }
}
