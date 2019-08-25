package com.changgou.order.service;

import com.changgou.order.pojo.OrderItem;

import java.util.List;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.order.service
 ****/
public interface CartService {


    /***
     * 购物车列表
     * @param username
     */
    List<OrderItem> list(String username);


    /***
     * 添加购物车
     * @param username
     * @param id
     * @param num
     */
    void addCart(String username,Long id,Integer num);
}
