package com.sky.state;

import com.sky.entity.Orders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 订单状态机测试。
 * 核心思路：期望值在这里硬编码成一张矩阵，不从 OrderEvent 反推 ——
 * 反推等于自己证明自己，规则写错了测试也跟着错。
 */
@DisplayName("订单状态机")
class OrderStateMachineTest {

    @DisplayName("6 个状态 × 9 个事件共 54 组组合必须与期望矩阵一致")
    @ParameterizedTest(name = "状态{0} 收到事件{1} 允许={2}")
    @CsvSource({
            // from, event, expected
            "1, PAY_SUCCESS,     true",
            "1, CONFIRM,         false",
            "1, REJECT,          false",
            "1, ADMIN_CANCEL,    false",
            "1, DELIVER,         false",
            "1, COMPLETE,        false",
            "1, USER_CANCEL,     true",
            "1, TIMEOUT_CANCEL,  true",
            "1, AUTO_COMPLETE,   false",

            "2, PAY_SUCCESS,     false",
            "2, CONFIRM,         true",
            "2, REJECT,          true",
            "2, ADMIN_CANCEL,    true",
            "2, DELIVER,         false",
            "2, COMPLETE,        false",
            "2, USER_CANCEL,     true",
            "2, TIMEOUT_CANCEL,  false",
            "2, AUTO_COMPLETE,   false",

            "3, PAY_SUCCESS,     false",
            "3, CONFIRM,         false",
            "3, REJECT,          false",
            "3, ADMIN_CANCEL,    true",
            "3, DELIVER,         true",
            "3, COMPLETE,        false",
            "3, USER_CANCEL,     false",
            "3, TIMEOUT_CANCEL,  false",
            "3, AUTO_COMPLETE,   false",

            "4, PAY_SUCCESS,     false",
            "4, CONFIRM,         false",
            "4, REJECT,          false",
            "4, ADMIN_CANCEL,    false",
            "4, DELIVER,         false",
            "4, COMPLETE,        true",
            "4, USER_CANCEL,     false",
            "4, TIMEOUT_CANCEL,  false",
            "4, AUTO_COMPLETE,   true",

            "5, PAY_SUCCESS,     false",
            "5, CONFIRM,         false",
            "5, REJECT,          false",
            "5, ADMIN_CANCEL,    false",
            "5, DELIVER,         false",
            "5, COMPLETE,        false",
            "5, USER_CANCEL,     false",
            "5, TIMEOUT_CANCEL,  false",
            "5, AUTO_COMPLETE,   false",

            "6, PAY_SUCCESS,     false",
            "6, CONFIRM,         false",
            "6, REJECT,          false",
            "6, ADMIN_CANCEL,    false",
            "6, DELIVER,         false",
            "6, COMPLETE,        false",
            "6, USER_CANCEL,     false",
            "6, TIMEOUT_CANCEL,  false",
            "6, AUTO_COMPLETE,   false"
    })
    void matrixMatchesExpectation(Integer from, OrderEvent event, boolean expected) {
        assertEquals(expected, OrderStateMachine.canTransfer(from, event),
                () -> "状态 " + from + " 收到事件 " + event + " 的判定不符合预期");
    }

    @DisplayName("已完成订单不能再被任何事件改变 —— 这是本次要补的核心漏洞")
    @Test
    void completedOrderRejectsEverything() {
        for (OrderEvent event : OrderEvent.values()) {
            assertFalse(OrderStateMachine.canTransfer(Orders.COMPLETED, event),
                    () -> "已完成订单竟然允许事件 " + event);
        }
    }

    @DisplayName("已取消订单同样是终态")
    @Test
    void cancelledOrderRejectsEverything() {
        for (OrderEvent event : OrderEvent.values()) {
            assertFalse(OrderStateMachine.canTransfer(Orders.CANCELLED, event),
                    () -> "已取消订单竟然允许事件 " + event);
        }
    }

    @DisplayName("派送中不允许商家取消")
    @Test
    void deliveringOrderCannotBeCancelledByAdmin() {
        assertFalse(OrderStateMachine.canTransfer(Orders.DELIVERY_IN_PROGRESS, OrderEvent.ADMIN_CANCEL));
        assertFalse(OrderStateMachine.canTransfer(Orders.DELIVERY_IN_PROGRESS, OrderEvent.USER_CANCEL));
    }

    @DisplayName("targetOf 对合法流转返回目标状态")
    @Test
    void targetOfReturnsTargetStatus() {
        assertEquals(Orders.CONFIRMED,
                OrderStateMachine.targetOf(Orders.TO_BE_CONFIRMED, OrderEvent.CONFIRM));
        assertEquals(Orders.CANCELLED,
                OrderStateMachine.targetOf(Orders.TO_BE_CONFIRMED, OrderEvent.REJECT));
        assertEquals(Orders.COMPLETED,
                OrderStateMachine.targetOf(Orders.DELIVERY_IN_PROGRESS, OrderEvent.COMPLETE));
    }

    @DisplayName("targetOf 对非法流转返回 null")
    @Test
    void targetOfReturnsNullForIllegalTransfer() {
        assertNull(OrderStateMachine.targetOf(Orders.COMPLETED, OrderEvent.ADMIN_CANCEL));
        assertNull(OrderStateMachine.targetOf(Orders.PENDING_PAYMENT, OrderEvent.DELIVER));
    }

    @SuppressWarnings("ConstantConditions")
    @DisplayName("null 入参不抛异常")
    @Test
    void nullSafe() {
        assertFalse(OrderStateMachine.canTransfer(null, OrderEvent.CONFIRM));
        assertFalse(OrderStateMachine.canTransfer(Orders.TO_BE_CONFIRMED, null));
        assertNull(OrderStateMachine.targetOf(null, OrderEvent.CONFIRM));
        assertNull(OrderStateMachine.targetOf(Orders.TO_BE_CONFIRMED, null));
    }

    @DisplayName("每个事件至少有一条合法的前置状态，防止定义了永远走不到的事件")
    @Test
    void everyEventIsReachable() {
        for (OrderEvent event : OrderEvent.values()) {
            boolean reachable = false;
            for (Integer status : new Integer[]{
                    Orders.PENDING_PAYMENT, Orders.TO_BE_CONFIRMED, Orders.CONFIRMED,
                    Orders.DELIVERY_IN_PROGRESS, Orders.COMPLETED, Orders.CANCELLED}) {
                if (OrderStateMachine.canTransfer(status, event)) {
                    reachable = true;
                    break;
                }
            }
            assertTrue(reachable, () -> "事件 " + event + " 没有任何状态可以触发它");
        }
    }
}
