package com.sky.service;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    /**
     * 用户下单
     * @param ordersSubmitDTO 订单提交信息
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);
    /**
     * 订单支付
     * @param ordersPaymentDTO 订单支付信息
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo 订单号
     */
    void paySuccess(String outTradeNo);

    /**
     * 用户端订单分页查询
     * @param page 页码
     * @param pageSize 每页数量
     * @param status 订单状态
     */
    PageResult pageQuery4User(int page, int pageSize, Integer status);

    /**
     * 查询订单详情
     * @param id 订单id
     */
    OrderVO details(Long id);

    /**
     * 用户取消订单
     * @param id 订单id
     */
    void userCancelById(Long id) throws Exception;

    /**
     * 再来一单
     * @param id 订单id
     */
    void repetition(Long id);

    /**
     * 订单搜索
      * @param ordersPageQueryDTO 订单搜索条件
     * @return 订单搜索结果
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各个状态的订单数量统计
     * @return 订单数量统计结果
     */
    OrderStatisticsVO statistics();
}
