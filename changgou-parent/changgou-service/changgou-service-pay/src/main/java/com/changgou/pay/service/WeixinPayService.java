package com.changgou.pay.service;

import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.pay.service
 ****/
public interface WeixinPayService {

    /***
     * 订单支付状态查询
     * @param outtradeno : 订单号
     */
    Map<String,String> queryPayStatus(String outtradeno) throws Exception;

    /***
     * 公众账号ID	appid
     * 商户号	mch_id
     * 随机字符串	nonce_str
     * 签名	sign
     * 商品描述	body
     * 商户订单号	out_trade_no[就是商品订单号]
     * 标价金额	total_fee
     * 终端IP	spbill_create_ip
     * 通知地址	notify_url
     * 交易类型	trade_type
     * outtradeno:订单号
     * totalfee:支付金额
     */
    //Map<String,String> createNative(String outtradeno, String totalfee) throws Exception;
    Map<String,String> createNative(Map<String,String> parameters) throws Exception;

}
