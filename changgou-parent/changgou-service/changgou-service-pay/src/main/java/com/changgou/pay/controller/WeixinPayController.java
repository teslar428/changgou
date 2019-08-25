package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.pay.controller
 ****/
@RestController
@RequestMapping(value = "/weixin/pay")
public class WeixinPayController {

    @Autowired
    private WeixinPayService weixinPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    /***
     * 支付信息回调地址
     */
    @RequestMapping(value = "/notifyurl")
    public String notifyurl(HttpServletRequest request) throws Exception {
        //获取网络输入流
        ServletInputStream is = request.getInputStream();

        //将网络输入流写入到本地对象或者本地文件中->OutputStreamXXX
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        //缓冲区
        byte[] buffer = new byte[1024];
        int len = 0;
        //读数据，然后输入到os中
        while ((len=is.read(buffer))!=-1){
            os.write(buffer,0,len);
        }
        //关闭资源
        os.flush();
        os.close();
        is.close();

        //返回的数据结果集[用户支付信息记录]
        String result = new String(os.toByteArray(),"UTF-8");
        System.out.println(result);
        Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
        System.out.println(resultMap);

        //从附加数据中获取对应的队列信息
        Map<String,String> attachMap = JSON.parseObject(resultMap.get("attach"),Map.class);
        String exchange = env.getProperty(attachMap.get("exchange"));
        String key = env.getProperty(attachMap.get("routingkey"));

        //String exchange = env.getProperty("mq.pay.exchange.order");
        //String key = env.getProperty("mq.pay.routing.key");
        rabbitTemplate.convertAndSend(exchange,key, JSON.toJSONString(resultMap));
        String responseResult = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        return responseResult;
    }

    /***
     * 查询支付状态
     * @param outtradeno
     * @return
     */
    @GetMapping(value = "/status/query")
    public Result queryStatus(String outtradeno) throws Exception{
        Map<String,String> resultMap = weixinPayService.queryPayStatus(outtradeno);
        return new Result(true,StatusCode.OK,"查询状态成功！",resultMap);
    }

    /****
     * 创建二维码支付
     * @return
     */
    @RequestMapping(value = "/create/native")
    //public Result<Map> createNative(String outtradeno, String money) throws Exception{
    public Result<Map> createNative(@RequestParam Map<String,String> parameters) throws Exception{
        //Map<String, String> resultMap = weixinPayService.createNative(outtradeno, money);
        Map<String, String> resultMap = weixinPayService.createNative(parameters);
        return new Result<Map>(true, StatusCode.OK,"创建支付二维码成功！",resultMap);
    }
}
