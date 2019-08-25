package com.changgou.seckill.task;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import entity.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.seckill.task
 * 作用：定时查询秒杀商品，存入到Redis缓存中
 ****/
@Component
public class SeckillGoodsPushTask {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /***
     * 30秒执行一次定定时任务
     * fixedDelay:距离上次执行结束后，N毫秒中再执行
     * fixedRate:距离上一个任务执行之后过N毫秒再次执行
     */
    //@Scheduled(cron = "0/5 * * * * ?")  //每5秒执行一次
    @Scheduled(cron = "0/15 * * * * ?")  //每5秒执行一次
    //@Scheduled(cron = "0,2,5 * * * * ?")  //0,2,5秒执行
    //@Scheduled(fixedDelay = 10000)
    //@Scheduled(fixedDelayString = "")
    //@Scheduled(fixedRate = 10000)
    //@Scheduled(fixedRateString ="${delay_time}")
    public void loadGoodsPushRedis(){
        //求出时间段菜单信息
        List<Date> dateMenus = DateUtil.getDateMenus();
        for (Date dateMenu : dateMenus) {
            //命名空间获取
            String namespace ="SeckillGoods_"+ DateUtil.data2str(dateMenu,DateUtil.PATTERN_YYYYMMDDHH);

            /****
             * 查询当前开始的秒杀商品
             * 往后面推移的4个时间点的秒杀商品数据都需要查询
             * 商品审核必须通过
             * 商品库存数量>0
             */

            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();

            //商品审核必须通过
            criteria.andEqualTo("status","1");

            //商品库存数量>0
            criteria.andGreaterThan("stockCount","0");

            //开始时间-结束时间           dateMenu =< star_time
            //                            end_time < dateMenu+2小时
            criteria.andGreaterThanOrEqualTo("startTime",dateMenu);
            criteria.andLessThan("endTime",DateUtil.addDateHour(dateMenu,2));

            //排除Redis中已经存在的秒杀商品,先获取namespace中所有商品的key  List<Id>   id not in()
            Set ids = redisTemplate.boundHashOps(namespace).keys();
            if(ids!=null && ids.size()>0){
                criteria.andNotIn("id",ids);
            }

            //执行查询
            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);
            //System.out.println("处理的商品个数："+seckillGoods.size());

            /***
             * Hash
             *      namespace               2028081916
             *          key SeckillGoods              1   SeckillGoods1
             *          key SeckillGoods              5   SeckillGoods5
             *          key SeckillGoods              19  SeckillGoods19
             *
             *      namespace               2028081918
             *          key SeckillGoods              11   SeckillGoods11
             *          key SeckillGoods              51   SeckillGoods51
             *          key SeckillGoods              191  SeckillGoods191
             */

            for (SeckillGoods seckillGood : seckillGoods) {
                //完整数据保存
                redisTemplate.boundHashOps(namespace).put(seckillGood.getId(),seckillGood);

                //商品个数队列创建 seckillGood.getStockCount() = 5
                redisTemplate.boundListOps("SeckillGoodsCountList_"+seckillGood.getId()).leftPushAll(pushIds(seckillGood.getStockCount(),seckillGood.getId()));

                //给每个商品添加一个计数器
                redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillGood.getId(),seckillGood.getStockCount());
            }
        }
    }


    /****
     * 组装商品ID数组
     */
    public Long[] pushIds(Integer count, Long id){
        Long[] ids = new Long[count];
        for (int i = 0; i <ids.length ; i++) {
            ids[i]=id;
        }
        return ids;
    }
}
