package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 更新商品数量
      * @param shoppingCart 购物车对象
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
      * @param shoppingCart 购物车对象
     */
    @Insert("insert into shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            " values (#{name},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{image},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 条件查询
     * @param shoppingCart 购物车对象
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据用户id删除购物车数据
      * @param currentId 当前用户id
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long currentId);

    /**
     * 根据购物车记录的主键id删除
     * @param id 购物车记录主键
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    /**
     * 批量插入购物车数据
      * @param shoppingCartList 购物车数据列表
     */
    void insertBatch(List<ShoppingCart> shoppingCartList);
}
