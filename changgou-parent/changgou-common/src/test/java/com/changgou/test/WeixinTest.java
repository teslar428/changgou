package com.changgou.test;

import com.github.wxpay.sdk.WXPayUtil;

import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.test
 * 微信操作
 ****/
public class WeixinTest {

    public static void main(String[] args) throws Exception{
        //生成随机字符
        String str = WXPayUtil.generateNonceStr();
        System.out.println(str);

        //XML字符串转成Map
        String xmlParams = "<xml><name>王五</name><age>27</age></xml>";
        Map<String, String> map = WXPayUtil.xmlToMap(xmlParams);
        System.out.println(map);

        //Map转成XML字符串
        map.put("school","黑马训练营！");
        String xmlstr = WXPayUtil.mapToXml(map);
        System.out.println(xmlstr);

        //将Map参数生成一个签名,将Map生成XML字符串
        String xmlParameters = WXPayUtil.generateSignedXml(map,"ITCAST");
        System.out.println(xmlParameters);


        String xmldemo = "<xml><appid><![CDATA[wx8397f8696b538317]]></appid><bank_type><![CDATA[CMB_CREDIT]]></bank_type><cash_fee><![CDATA[1]]></cash_fee><fee_type><![CDATA[CNY]]></fee_type><is_subscribe><![CDATA[N]]></is_subscribe><mch_id><![CDATA[1473426802]]></mch_id><nonce_str><![CDATA[1b36ea1ab5754071be89bf75fd64f522]]></nonce_str><openid><![CDATA[oNpSGwbLujgmJYFgcB_BoboFhecQ]]></openid><out_trade_no><![CDATA[1163030316416237569]]></out_trade_no><result_code><![CDATA[SUCCESS]]></result_code><return_code><![CDATA[SUCCESS]]></return_code><sign><![CDATA[ADE84E70FC9902A6EAC790AFAE4D7BF7]]></sign><time_end><![CDATA[20190818181442]]></time_end><total_fee>1</total_fee><trade_type><![CDATA[NATIVE]]></trade_type><transaction_id><![CDATA[4200000344201908189436900741]]></transaction_id></xml>";
        Map<String, String> mapinfo = WXPayUtil.xmlToMap(xmldemo);
        System.out.println(mapinfo);
    }
}
