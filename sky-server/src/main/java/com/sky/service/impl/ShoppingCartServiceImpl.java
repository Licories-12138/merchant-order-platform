package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;


    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        // 只能查询自己的购物车数据
        shoppingCart.setUserId(BaseContext.getCurrentId());

        // 判断当前商品是否在购物车中
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        if (shoppingCartList != null && !shoppingCartList.isEmpty()) {
            //如果已经存在，就更新数量，数量加1
            ShoppingCart existShoppingCart = shoppingCartList.get(0);
            existShoppingCart.setNumber(existShoppingCart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(existShoppingCart);
        }else {
            //如果不存在，插入数据，数量就是1
            //判断当前添加到购物车的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null) {
                //添加的是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {
                //添加的是套餐
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查看购物车
     * @return 购物车列表
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        return shoppingCartMapper.list(ShoppingCart.
                builder()
                .userId(BaseContext.getCurrentId())
                .build());
    }

    /**
     * 清空购物车
     */
    @Override
    public void cleanShoppingCart() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    /**
     * 减少购物车商品
     * @param shoppingCartDTO
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        // 1. 构建查询条件：当前用户 + 菜品/套餐信息
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .dishId(shoppingCartDTO.getDishId())
                .setmealId(shoppingCartDTO.getSetmealId())
                .dishFlavor(shoppingCartDTO.getDishFlavor())
                .build();

        // 2. 查询当前用户的这条购物车记录
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        // 3. 判空：如果查不到记录，直接返回，避免空指针
        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            // 4. 判断数量
            if (cart.getNumber() > 1) {
                // 数量大于 1，数量减一，并更新数据库
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(cart);
            } else {
                // 数量等于 1，直接删除这条购物车记录
                shoppingCartMapper.deleteById(cart.getId());
            }
        }
    }
}
