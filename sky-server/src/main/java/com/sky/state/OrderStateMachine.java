package com.sky.state;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单状态机：集中回答「某个状态能不能转到另一个状态」。
 * 数据来源是 {@link OrderEvent}，本类只负责把它展开成一张可查询的表。
 * 边界说明：本类只管状态流转是否合法，不执行任何业务动作 ——
 * 退款、写取消原因、更新支付状态这些副作用留在 OrderServiceImpl 里做。
 */
public final class OrderStateMachine {

    // 索引结构：前置状态 -> (事件 -> 目标状态)。
    // 为什么用事件当第二层键，而不是目标状态：同一个目标状态可以被多个事件到达，
    // 比如「已取消」同时被拒单、商家取消、用户取消、支付超时四个事件指向，
    // 用目标状态当键会让它们互相覆盖。
    private static final Map<Integer, Map<OrderEvent,Integer>> TABLE = buildTable();

    /**
     * 把 OrderEvent 里的定义展开成查询表。
     * 最终得到「前置 → 目标 → 事件」的完整映射关系表
     *
     * @return 流转表
     */
    private static Map<Integer, Map<OrderEvent, Integer>> buildTable() {
        // 1. 建一个空的 HashMap 作为结果
        Map<Integer, Map<OrderEvent, Integer>> result = new HashMap<>();
        // 2. 遍历 OrderEvent.values()，拿到每个事件 event
        for (OrderEvent event : OrderEvent.values()) {
            // 3. 再遍历 event.getFrom() 里的每个前置状态 from
            for (Integer from : event.getFrom()) {
                // 4. 放进结果
                result.computeIfAbsent(from, k -> new HashMap<>()).put(event, event.getTo());
            }
        }
        return result;
    }

    /**
     * 判断能否从 fromStatus 流转到 toStatus。
     *
     * @param fromStatus 订单当前状态
     * @param event      触发的事件
     * @return true 表示允许
     */
    public static boolean canTransfer(Integer fromStatus, OrderEvent event) {
        return fromStatus != null && event != null && event.matchFrom(fromStatus);
    }

    // 目标状态：改完状态要用它，别在 Service 里再写一遍常量
    public static Integer targetOf(Integer fromStatus, OrderEvent event) {
        if (event == null || !canTransfer(fromStatus, event)) {
            return null;
        }
        return event.getTo();
    }

    private OrderStateMachine() {
    }
}
