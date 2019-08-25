package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.order.service.impl
 ****/
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private RedisTemplate redisTemplate;

    /***
     * 购物车列表查询
     * @param username
     * @return
     */
    @Override
    public List<OrderItem> list(String username) {
        //Hash类型   获取它里面所有数据  boundHashOps(key).values()
        //Hash类型   获取它里面所有key  boundHashOps(key).keys()
        return redisTemplate.boundHashOps("Cart_"+username).values();
    }

    /****
     * 添加购物车
     * @param username
     * @param id  [SkuId]
     * @param num
     */
    @Override
    public void addCart(String username, Long id, Integer num) {
        //判断购买数量是否<=0，移除该商品
        if(num<=0){
            redisTemplate.boundHashOps("Cart_"+username).delete(id);
            return;
        }

        //完善OrderItme
        //根据SkuID查询Sku信息
        Result<Sku> resultSku = skuFeign.findById(id);
        Sku sku = resultSku.getData();

        //3级分类->Spu中,查询Spu
        Result<Spu> resultSpu = spuFeign.findById(sku.getSpuId());
        Spu spu = resultSpu.getData();

        OrderItem orderItem = new OrderItem();
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId());
        orderItem.setSkuId(id);
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(orderItem.getPrice()*num);
        orderItem.setImage(spu.getImage());
        orderItem.setIsReturn("0");
        //构建一个商品购物车明细对象  OrderItem

        /***
         * 将该对象添加到用户的购物车中
         * key:username
         * List<Order>
         *
         *  username[namespace]
         *          1->OrderItem
         *          5->OrderItem
         *          8->OrderItem
         *          111->OrderItem
         *  lisi[namespace]
         *          1->OrderItem
         *          5->OrderItem
         *          8->OrderItem
         *          111->OrderItem
         */
        redisTemplate.boundHashOps("Cart_"+username).put(id,orderItem);
    }
}
