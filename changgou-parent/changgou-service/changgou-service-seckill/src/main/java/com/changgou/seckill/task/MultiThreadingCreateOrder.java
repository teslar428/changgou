package com.changgou.seckill.task;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.util.SeckillStatus;
import entity.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.seckill.task
 * 异步下单
 ****/
@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /***
     * @Async：需要让该方法异步执行，则只需要在该方法上加一个@Async注解即可
     */
    @Async
    public void createOrder(){
        //从队列中获取用户排队信息,rightPop()从队列右边取出第1条数据
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();

        if(seckillStatus!=null){
            //从队列中获取商品对象[id]
            Object sgood = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).rightPop();

            //没有商品了，则不下单
            if(sgood==null){
                System.out.println("--------"+seckillStatus.getUsername()+"没有抢到单！");
                //重复排队标识
                redisTemplate.boundHashOps("UserQueueCount").delete(seckillStatus.getUsername());

                //清理用户排队信息,用于查询用户抢单状态
                redisTemplate.boundHashOps("UserQueueStatus").delete(seckillStatus.getUsername());  //用户查询的状态信息
                return;
            }

            //测试，先硬编码
            String username = seckillStatus.getUsername();
            String time=seckillStatus.getTime();
            Long id = seckillStatus.getGoodsId();

            //查询商品数据-Redis
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(id);

            //补全数据
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());
            seckillOrder.setMoney(seckillGoods.getCostPrice()); //查询商品信息
            seckillOrder.setUserId(username);
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setStatus("0");    //未支付

            //将秒杀订单存入到MySQL[Redis]-一个用户只允许存在一个未支付的秒杀订单-key = username  value = SeckillOrder
            redisTemplate.boundHashOps("SeckillOrder").put(username,seckillOrder);

            //判断商品库存是否=0,使用Redis递增(-1)实现递减操作
            Long surplusCount = redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillGoods.getId(), -1);
            //递减库存
            seckillGoods.setStockCount(surplusCount.intValue());
            //如果商品数量=0，则同步数据到MySQL中
            //if(seckillGoods.getStockCount()==0){
            if(surplusCount==0){
                //同步数据到MySQL中
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                //删除缓存中的数据
                redisTemplate.boundHashOps("SeckillGoods_" + time).delete(id);
            }else{
                //修改Redis中的数据
                redisTemplate.boundHashOps("SeckillGoods_" + time).put(id,seckillGoods);
            }

            //更新seckillStatus状态
            seckillStatus.setOrderId(seckillOrder.getId());
            seckillStatus.setMoney(Float.valueOf(seckillOrder.getMoney())); //订单金额
            seckillStatus.setStatus(2); //下单成功，等待支付
            redisTemplate.boundHashOps("UserQueueStatus").put(username,seckillStatus);  //更新用户查询的状态信息
        }
    }
}
