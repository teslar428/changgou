package com.changgou.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.seckill.mq
 * 监听秒杀订单支付状态信息
 ****/
@Component
@RabbitListener(queues = {"${mq.pay.queue.seckillorder}"})
public class SeckillOrderMessageListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /***
     * 监听支付信息
     * @param msg
     */
    @RabbitHandler
    public void getSeckillPayMessage(String msg) throws Exception {
        //将消息信息转成Map
        Map<String,String> payMap = JSON.parseObject(msg,Map.class);

        //return_code  通信状态
        String return_code = payMap.get("return_code");

        if(return_code.equalsIgnoreCase("success")){
            //result_code  业务结果
            String result_code = payMap.get("result_code");
            //attach   自定义参数
            String attach = payMap.get("attach");
            Map<String,String> attachMap = JSON.parseObject(attach,Map.class);
            String username = attachMap.get("username");
            if(username==null){
                return;
            }

            //支付成功
            if(result_code.equalsIgnoreCase("success")){
                //transaction_id  微信支付订单号
                String transaction_id = payMap.get("transaction_id");
                //支付时间
                String time_end = payMap.get("time_end");

                //修改订单状态->Redis ->username? 支付时间[腾讯返回的],交易流水号
                seckillOrderService.updateStatus(username,time_end,transaction_id);
            }else{
                //支付失败，删除订单，回滚库存
                seckillOrderService.deleteOrder(username);
            }
        }
    }

}
