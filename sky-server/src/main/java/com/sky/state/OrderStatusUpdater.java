package com.sky.state;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * 订单状态流转执行器：把「改状态」这件事收口到一个地方。
 * 为什么不在 OrderServiceImpl 里写成私有方法：
 * OrderTask 里的两个定时任务也要改状态，私有方法它够不着，
 * 结果就是定时任务继续绕过校验。
 * 职责边界：本类只负责「校验 + 落库 + 记日志」，
 * 不负责退款、推送之类的业务副作用 —— 那些由调用方在调用之后执行。
 */
@Component
@Slf4j
public class OrderStatusUpdater {
    private final OrderMapper orderMapper;

    public OrderStatusUpdater(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    /**
     * 把订单从当前状态流转到事件对应的目标状态。
     *
     * @param current 从库里查出来的订单，必须带 id 和 status
     * @param event   本次触发的事件
     * @param extra   顺带要写的字段，没有传 null
     */
    public void transfer(Orders current, OrderEvent event, Consumer<Orders> extra) {
        Integer targetStatus = OrderStateMachine.targetOf(current.getStatus(), event);
        if (targetStatus == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders update = new Orders();
        update.setId(current.getId());
        update.setStatus(targetStatus);
        if (extra != null) {
            extra.accept(update);
        }

        int rows = orderMapper.updateStatusWithExpected(update, current.getStatus());
        if (rows == 0) {
            // 能走到这里说明状态机放行了，rows=0 只可能是并发抢改
            log.warn("订单状态并发冲突，id={}，期望状态={}，目标状态={}",
                    current.getId(), current.getStatus(), targetStatus);
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_CHANGED);
        }

        log.info("订单id：{}，旧状态：{} -> 新状态：{}，事件：{}",
                update.getId(), current.getStatus(), targetStatus, event);
    }

    /** 不需要额外字段的流转，省得调用方写 null */
    public void transfer(Orders current, OrderEvent event) {
        transfer(current, event, null);
    }
}
