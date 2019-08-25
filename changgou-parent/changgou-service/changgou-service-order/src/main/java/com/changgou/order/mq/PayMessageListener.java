package com.changgou.order.mq;

import com.alibaba.fastjson.JSON;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.order.mq
 ****/
@Component
@RabbitListener(queues = "${mq.pay.queue.order}")
public class PayMessageListener {

    @Autowired
    private OrderService orderService;

    /***
     * 监听消息
     */
    @RabbitHandler
    public void getOrderMessage(String msg){
        //将消息转成Map
        Map<String,String> resultMap = JSON.parseObject(msg,Map.class);
        String return_code = resultMap.get("return_code");//通信标识
        if(return_code.equalsIgnoreCase("success")){
            //获取数据判断是否支付成功
            //订单号
            String out_trade_no = resultMap.get("out_trade_no");
            //用户支付业务结果  SUCCESS:成功  FAIL:失败
            String result_code = resultMap.get("result_code");
            //支付成功，需改订单状态
            if(result_code.equalsIgnoreCase("success")){
                //修改订单状态
                orderService.updateStatus(out_trade_no,resultMap.get("transaction_id"));
            }else{
                //支付失败，删除订单[状态修改]
                orderService.deleteOrdder(out_trade_no);
            }
        }
    }

}
