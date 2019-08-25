package com.changgou.order.controller;

import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.plaf.nimbus.State;
import java.util.List;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.order.controller
 * 购物车功能
 ****/
@RestController
@RequestMapping(value = "/cart")
@CrossOrigin
public class CartController {

    @Autowired
    private CartService cartService;

    /****
     * 获取用户购物车集合
     */
    @GetMapping(value = "/list")
    public Result<List<OrderItem>> list(){
        //username=wangwu
        String username = TokenDecode.getUserInfo().get("username");
        List<OrderItem> orderItems = cartService.list(username);
        return new Result<List<OrderItem>>(true,StatusCode.OK,"查询成功！",orderItems);
    }

    /****
     * 添加购物车
     */
    @GetMapping(value = "/add")
    public Result addCart(Long id,Integer num){
        //加入购物车
        String username=TokenDecode.getUserInfo().get("username");
        cartService.addCart(username,id,num);
        return new Result(true, StatusCode.OK,"添加购物车成功！");
    }

}
