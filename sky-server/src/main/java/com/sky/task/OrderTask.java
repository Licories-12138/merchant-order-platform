package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    public void processTimeoutOrder() {
        log.info("处理支付超时订单：{}", LocalDateTime.now() );

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        // select * from orders where status = 1 and order_time < 当前时间-15分钟
        List<Orders> ordersList = orderMapper.getByStatusAndOrdertimeLT(1, time);
        if(ordersList != null && !ordersList.isEmpty()) { // 判断订单列表是否为空
            ordersList.forEach(order -> {
                order.setStatus(Orders.CANCELLED); // 订单状态改为已取消
                order.setCancelReason("支付超时，自动取消");
                order.setCancelTime(LocalDateTime.now()); // 取消时间
                orderMapper.update(order);
            });
        }
    }

    /**
     * 处理“ 派送中 ”状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
    public void processDeliveryOrder(){
        log.info("处理派送中订单：{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        // select * from orders where status = 2 and order_time < 当前时间-60分钟
        List<Orders> ordersList = orderMapper.getByStatusAndOrdertimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if(ordersList != null && !ordersList.isEmpty()){
            ordersList.forEach(order -> {
                order.setStatus(Orders.COMPLETED); // 订单状态改为已完成
                orderMapper.update(order);
            });
        }
    }

}
