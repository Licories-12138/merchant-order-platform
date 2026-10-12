package com.sky.state;

import com.sky.entity.Orders;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 订单状态流转事件。
 *
 * 一条记录 = 一个业务动作：从哪些前置状态（from）流转到哪个目标状态（to）。
 * from 统一用 Set，哪怕只有一个来源也这样 —— 省得写「单个值」和「多个值」两套判断逻辑。
 *
 * 维护约定：新增任何改订单状态的地方，都必须在这里加一条，不允许在别处直接 setStatus。
 */
@SuppressWarnings("JavadocBlankLines")
@Getter
public enum OrderEvent {

    PAY_SUCCESS(set(Orders.PENDING_PAYMENT), Orders.TO_BE_CONFIRMED, "支付成功"),

    CONFIRM(set(Orders.TO_BE_CONFIRMED), Orders.CONFIRMED, "商家接单"),

    REJECT(set(Orders.TO_BE_CONFIRMED), Orders.CANCELLED, "商家拒单"),

    // 派送中(4)、已完成(5) 不允许商家取消，只能走 2 待接单 / 3 已接单
    ADMIN_CANCEL(set(Orders.TO_BE_CONFIRMED, Orders.CONFIRMED), Orders.CANCELLED, "商家取消"),

    DELIVER(set(Orders.CONFIRMED), Orders.DELIVERY_IN_PROGRESS, "商家派送"),

    COMPLETE(set(Orders.DELIVERY_IN_PROGRESS), Orders.COMPLETED, "完成订单"),

    USER_CANCEL(set(Orders.PENDING_PAYMENT, Orders.TO_BE_CONFIRMED), Orders.CANCELLED, "用户取消"),

    TIMEOUT_CANCEL(set(Orders.PENDING_PAYMENT), Orders.CANCELLED, "支付超时自动取消"),

    AUTO_COMPLETE(set(Orders.DELIVERY_IN_PROGRESS), Orders.COMPLETED, "超时自动完成");

    // 允许的前置状态集合
    private final Set<Integer> from;

    // 目标状态
    private final Integer to;

    // 事件描述，用于日志
    private final String desc;

    OrderEvent(Set<Integer> from, Integer to, String desc) {
        this.from = from;
        this.to = to;
        this.desc = desc;
    }

    /**
     * 判断某个当前状态能否触发本事件。
     *
     * @param fromStatus 订单当前状态
     * @return true 表示允许流转
     */
    public boolean matchFrom(Integer fromStatus) {
        return from != null && from.contains(fromStatus);
    }

    /**
     * 构造不可变 Set。
     *
     * 为什么不用 Set.of：那是 Java 9+ 的 API。本项目按 Java 8 目标编译，
     * 用高版本 JDK 编译时能通过，换到 Java 8 运行时会 NoSuchMethodError。
     *
     * @param statuses 状态值
     * @return 不可变 Set
     */
    private static Set<Integer> set(Integer... statuses) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(statuses)));
    }
}
