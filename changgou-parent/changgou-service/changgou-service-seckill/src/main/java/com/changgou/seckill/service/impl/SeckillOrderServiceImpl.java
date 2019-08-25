package com.changgou.seckill.service.impl;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SeckillOrderService;
import com.changgou.seckill.task.MultiThreadingCreateOrder;
import com.changgou.seckill.util.SeckillStatus;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/****
 * @Author:www.itheima.com
 * @Description:SeckillOrder业务层接口实现类
 * @Date 黑马畅购商城
 *****/
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /***
     * 查询抢单状态
     * @param username
     * @return
     */
    @Override
    public SeckillStatus queryStatus(String username) {
        return (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
    }


    /***
     * 用户支付失败，删除订单信息
     * @param username
     */
    @Override
    public void deleteOrder(String username) {
        //查询排队信息
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);

        //根据订单购买的商品，找到对应的商品数据  商品存储  namespace(time) + id
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_"+seckillStatus.getTime()).get(seckillStatus.getGoodsId());
        //如果seckillGoods==null
        if(seckillGoods==null){
            //从数据加载该商品->0->Redis->1
            seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillStatus.getGoodsId());
        }

        //商品数量递增-库存回滚->计数器、队列增加一个数据
        redisTemplate.boundListOps("SeckillGoodsCountList_"+seckillStatus.getGoodsId()).leftPush(seckillGoods.getId());
        Long count = redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillGoods.getId(), 1);
        seckillGoods.setStockCount(count.intValue());

        //删除排队信息
        redisTemplate.boundHashOps("UserQueueCount").delete(username);  //防止重复派单标识
        redisTemplate.boundHashOps("UserQueueStatus").delete(username); //派单状态
    }

    /***
     * username:用户名
     * endtime:交易时间
     * transactionid:交易流水号
     * 修改订单状态
     */
    @Override
    public void updateStatus(String username, String endtime, String transactionid) throws Exception {
        //1.查询出订单
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);

        if(seckillOrder==null){
            return;
        }

        //2.将订单状态和其他参数修改一下
        seckillOrder.setStatus("1");    //已支付
        seckillOrder.setTransactionId(transactionid);
        //交易时间转换
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        seckillOrder.setPayTime(simpleDateFormat.parse(endtime));

        //3.将订单数据存入到MySQL
        seckillOrderMapper.insertSelective(seckillOrder);

        //4.删除订单信息
        redisTemplate.boundHashOps("SeckillOrder").delete(username);

        //5.删除用户排队信息
        redisTemplate.boundHashOps("UserQueueCount").delete(username);  //防止重复派单标识
        redisTemplate.boundHashOps("UserQueueStatus").delete(username); //派单状态
    }

    /***
     * 添加订单
     */
    @Override
    public Boolean add(Long id, String time, String username){
        /***
         * 记录当前用户下单信息
         * increment(key,增量)  实现对指定键数据进行递增
         */
        Long count = redisTemplate.boundHashOps("UserQueueCount").increment(username, 1);
        if(count>1){
            //100表示重复排队
            throw new RuntimeException("100");
        }


        //创建用户排队信息
        SeckillStatus seckillStatus = new SeckillStatus(username, new Date(),1,id,time);
        //boundListOps:List数据类型,leftPush:从左边存储
        redisTemplate.boundListOps("SeckillOrderQueue").leftPush(seckillStatus);

        //用于查询用户抢单状态
        redisTemplate.boundHashOps("UserQueueStatus").put(username,seckillStatus);  //用户查询的状态信息

        //异步下单
        multiThreadingCreateOrder.createOrder();

        return true;
    }



    /**
     * SeckillOrder条件+分页查询
     * @param seckillOrder 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(seckillOrder);
        //执行搜索
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectByExample(example));
    }

    /**
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<SeckillOrder> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectAll());
    }

    /**
     * SeckillOrder条件查询
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder){
        //构建查询条件
        Example example = createExample(seckillOrder);
        //根据构建的条件查询数据
        return seckillOrderMapper.selectByExample(example);
    }


    /**
     * SeckillOrder构建查询对象
     * @param seckillOrder
     * @return
     */
    public Example createExample(SeckillOrder seckillOrder){
        Example example=new Example(SeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if(seckillOrder!=null){
            // 主键
            if(!StringUtils.isEmpty(seckillOrder.getId())){
                    criteria.andEqualTo("id",seckillOrder.getId());
            }
            // 秒杀商品ID
            if(!StringUtils.isEmpty(seckillOrder.getSeckillId())){
                    criteria.andEqualTo("seckillId",seckillOrder.getSeckillId());
            }
            // 支付金额
            if(!StringUtils.isEmpty(seckillOrder.getMoney())){
                    criteria.andEqualTo("money",seckillOrder.getMoney());
            }
            // 用户
            if(!StringUtils.isEmpty(seckillOrder.getUserId())){
                    criteria.andEqualTo("userId",seckillOrder.getUserId());
            }
            // 创建时间
            if(!StringUtils.isEmpty(seckillOrder.getCreateTime())){
                    criteria.andEqualTo("createTime",seckillOrder.getCreateTime());
            }
            // 支付时间
            if(!StringUtils.isEmpty(seckillOrder.getPayTime())){
                    criteria.andEqualTo("payTime",seckillOrder.getPayTime());
            }
            // 状态，0未支付，1已支付
            if(!StringUtils.isEmpty(seckillOrder.getStatus())){
                    criteria.andEqualTo("status",seckillOrder.getStatus());
            }
            // 收货人地址
            if(!StringUtils.isEmpty(seckillOrder.getReceiverAddress())){
                    criteria.andEqualTo("receiverAddress",seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if(!StringUtils.isEmpty(seckillOrder.getReceiverMobile())){
                    criteria.andEqualTo("receiverMobile",seckillOrder.getReceiverMobile());
            }
            // 收货人
            if(!StringUtils.isEmpty(seckillOrder.getReceiver())){
                    criteria.andEqualTo("receiver",seckillOrder.getReceiver());
            }
            // 交易流水
            if(!StringUtils.isEmpty(seckillOrder.getTransactionId())){
                    criteria.andEqualTo("transactionId",seckillOrder.getTransactionId());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Long id){
        seckillOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder){
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void add(SeckillOrder seckillOrder){
        seckillOrderMapper.insert(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id){
        return  seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询SeckillOrder全部数据
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return seckillOrderMapper.selectAll();
    }
}
