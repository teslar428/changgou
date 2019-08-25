package com.changgou.seckill.service;

import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.util.SeckillStatus;
import com.github.pagehelper.PageInfo;

import java.text.ParseException;
import java.util.List;

/****
 * @Author:www.itheima.com
 * @Description:SeckillOrder业务层接口
 * @Date 黑马畅购商城
 *****/
public interface SeckillOrderService {

    /***
     * 用户支付失败，删除订单信息
     */
    void deleteOrder(String username);

    /***
     * username:用户名
     * endtime:交易时间
     * transactionid:交易流水号
     * 修改订单状态
     */
    void updateStatus(String username,String endtime,String transactionid) throws ParseException, Exception;

    /****
     * 添加订单
     * @param id
     * @param time
     * @param username
     * @return
     */
    Boolean add(Long id, String time, String username);

    /***
     * 根据用户名字查询排队状态
     * @param username
     */
    SeckillStatus queryStatus(String username);

    /***
     * SeckillOrder多条件分页查询
     * @param seckillOrder
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size);

    /***
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(int page, int size);

    /***
     * SeckillOrder多条件搜索方法
     * @param seckillOrder
     * @return
     */
    List<SeckillOrder> findList(SeckillOrder seckillOrder);

    /***
     * 删除SeckillOrder
     * @param id
     */
    void delete(Long id);

    /***
     * 修改SeckillOrder数据
     * @param seckillOrder
     */
    void update(SeckillOrder seckillOrder);

    /***
     * 新增SeckillOrder
     * @param seckillOrder
     */
    void add(SeckillOrder seckillOrder);

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
     SeckillOrder findById(Long id);

    /***
     * 查询所有SeckillOrder
     * @return
     */
    List<SeckillOrder> findAll();
}
