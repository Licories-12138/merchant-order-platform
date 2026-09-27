package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param order 订单
     */
    void insert(Orders order);
    /**
     * 根据订单号和用户id查询订单
     * @param orderNumber 订单号
     * @param userId 用户id
     */
    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 修改订单信息
     * @param orders 订单
     */
    void update(Orders orders);

    /**
     * 分页条件查询并按下单时间排序
      * @param ordersPageQueryDTO 订单分页查询DTO
     */
    Page<Orders> pageQuery4User(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
      * @param id 订单id
      * @return 订单
     */
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    /**
     * 根据状态统计订单数量
      * @param status 订单状态
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);
}
