package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 通过购物车信息动态查询符合条件的购物车信息
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 更新购物车信息
     * @param cartItem
     */
    void update(ShoppingCart cartItem);

    /**
     * 插入购物车数据
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time)" +
            "value (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 添加购物车
     * @param cartItem
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void addOrSub(ShoppingCart cartItem);

    /**
     * 清空用户的购物车
     * @param currentId
     */
    @Delete("delete from shopping_cart where user_id = #{currentId}")
    void cleanAll(Long currentId);

    /**
     * 删除购物车
     * @param cartItem
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void delete(ShoppingCart cartItem);
}
