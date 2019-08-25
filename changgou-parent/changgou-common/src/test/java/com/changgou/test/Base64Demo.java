package com.changgou.test;

import org.junit.Test;

import java.util.Base64;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.test
 ****/
public class Base64Demo {


    /***
     * Base64加密/解密
     */
    @Test
    public void testBase64EncodeAndDecode() throws Exception{
        String str = "www.itheima.com";
        byte[] encode = Base64.getEncoder().encode(str.getBytes());
        //String encodestr = new String(encode,"UTF-8");
        String encodestr = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        System.out.println(encodestr);

        byte[] decode = Base64.getDecoder().decode(encodestr);
        String decodestr = new String(decode,"UTF-8");
        System.out.println(decodestr);
    }
}
