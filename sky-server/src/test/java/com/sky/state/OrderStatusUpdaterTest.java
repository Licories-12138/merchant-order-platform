package com.sky.state;

import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 订单状态流转执行器测试。
 *
 * 用 Mockito 把 OrderMapper 换成假的，这样不用连数据库就能验证两件关键的事：
 * 1. 非法流转必须「一次数据库都不碰」—— 这是本次要补的漏洞的硬证据
 * 2. 并发冲突（影响行数为 0）必须抛异常，而不是静默当成功
 */
@DisplayName("订单状态流转执行器")
class OrderStatusUpdaterTest {

    private OrderMapper orderMapper;
    private OrderStatusUpdater updater;

    @BeforeEach
    void setUp() {
        orderMapper = mock(OrderMapper.class);
        updater = new OrderStatusUpdater(orderMapper);
    }

    private Orders orderOf(Long id, Integer status) {
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(status);
        return orders;
    }

    @DisplayName("合法流转：带上目标状态和期望的旧状态调用条件更新")
    @Test
    void legalTransferSendsCorrectSqlParams() {
        Orders current = orderOf(100L, Orders.TO_BE_CONFIRMED);
        when(orderMapper.updateStatusWithExpected(any(), eq(Orders.TO_BE_CONFIRMED))).thenReturn(1);

        updater.transfer(current, OrderEvent.CONFIRM);

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(orderMapper).updateStatusWithExpected(captor.capture(), eq(Orders.TO_BE_CONFIRMED));

        Orders sent = captor.getValue();
        assertEquals(100L, sent.getId());
        assertEquals(Orders.CONFIRMED, sent.getStatus());
    }

    @DisplayName("已完成订单取消：抛异常，且一次数据库都不碰")
    @Test
    void illegalTransferNeverTouchesDatabase() {
        Orders current = orderOf(100L, Orders.COMPLETED);

        assertThrows(OrderBusinessException.class,
                () -> updater.transfer(current, OrderEvent.ADMIN_CANCEL));

        verify(orderMapper, never()).updateStatusWithExpected(any(), any());
    }

    @DisplayName("派送中订单商家取消：同样被状态机拦住")
    @Test
    void deliveringOrderCannotBeCancelledByAdmin() {
        Orders current = orderOf(100L, Orders.DELIVERY_IN_PROGRESS);

        assertThrows(OrderBusinessException.class,
                () -> updater.transfer(current, OrderEvent.ADMIN_CANCEL));

        verify(orderMapper, never()).updateStatusWithExpected(any(), any());
    }

    @DisplayName("并发冲突：影响行数为 0 时抛异常，不当成功")
    @Test
    void conflictIsReportedAsFailure() {
        Orders current = orderOf(100L, Orders.DELIVERY_IN_PROGRESS);
        when(orderMapper.updateStatusWithExpected(any(), any())).thenReturn(0);

        assertThrows(OrderBusinessException.class,
                () -> updater.transfer(current, OrderEvent.COMPLETE));
    }

    @DisplayName("不带额外字段的重载不会 NPE")
    @Test
    void overloadWithoutExtraIsSafe() {
        Orders current = orderOf(100L, Orders.CONFIRMED);
        when(orderMapper.updateStatusWithExpected(any(), any())).thenReturn(1);

        assertDoesNotThrow(() -> updater.transfer(current, OrderEvent.DELIVER));
    }

    @DisplayName("额外字段确实写进了更新对象")
    @Test
    void extraFieldsAreApplied() {
        Orders current = orderOf(100L, Orders.DELIVERY_IN_PROGRESS);
        when(orderMapper.updateStatusWithExpected(any(), any())).thenReturn(1);

        updater.transfer(current, OrderEvent.COMPLETE, o -> o.setDeliveryTime(LocalDateTime.now()));

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(orderMapper).updateStatusWithExpected(captor.capture(), eq(Orders.DELIVERY_IN_PROGRESS));

        Orders sent = captor.getValue();
        assertEquals(Orders.COMPLETED, sent.getStatus());
        assertNotNull(sent.getDeliveryTime(), "送达时间没有被写进更新对象");
    }
}
