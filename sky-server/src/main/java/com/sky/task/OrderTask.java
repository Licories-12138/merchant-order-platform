package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;

import com.sky.state.OrderEvent;
import com.sky.state.OrderStatusUpdater;
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
    @Autowired
    private OrderStatusUpdater orderStatusUpdater;

    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    public void processTimeoutOrder() {
        log.info("处理支付超时订单：{}", LocalDateTime.now() );

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        // 查出「下单超过 15 分钟仍是待付款」的订单
        List<Orders> ordersList = orderMapper.getByStatusAndOrdertimeLT(Orders.PENDING_PAYMENT, time);
        if (ordersList == null || ordersList.isEmpty()){ //
            return;
        }

        // 用 for 而不是 forEach：单条失败要继续跑剩下的一批
        for (Orders order : ordersList) {
            try {
                orderStatusUpdater.transfer(order, OrderEvent.TIMEOUT_CANCEL, o -> {
                    o.setCancelReason("支付超时，自动取消");
                    o.setCancelTime(LocalDateTime.now());
                });
            }catch (Exception e){
                // 这里故意吞异常：被并发改掉的订单会让 CAS 返回 0 行，
                // 属于正常现象，记一条日志跳过即可，不能让整批中断
                log.warn("支付超时自动取消失败，订单id：{}，原因：{}", order.getId(), e.getMessage());
            }
        }
    }

    /**
     * 处理“ 派送中 ”状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
    public void processDeliveryOrder(){
        log.info("处理派送中订单：{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        // 查出「派送中超过 60 分钟」的订单
        List<Orders> ordersList = orderMapper.getByStatusAndOrdertimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if (ordersList == null || ordersList.isEmpty()){
            return;
        }
        for (Orders order : ordersList){
            try {
                orderStatusUpdater.transfer(order, OrderEvent.AUTO_COMPLETE);
            }catch (Exception e){
                log.warn("派送完成自动确认失败，订单id：{}，原因：{}", order.getId(), e.getMessage());
            }
        }
    }

}
