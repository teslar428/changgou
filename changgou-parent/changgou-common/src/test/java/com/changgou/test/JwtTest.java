package com.changgou.test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.test
 ****/
public class JwtTest {

    /***
     * 创建Jwt令牌
     */
    @Test
    public void testCreateToken(){
        //Jwt构建对象
        JwtBuilder jwtBuilder = Jwts.builder();
        jwtBuilder.setId("No0001"); //唯一ID
        jwtBuilder.setIssuedAt(new Date()); //令牌颁发时间
        jwtBuilder.setSubject("主题");   //主题
        jwtBuilder.setIssuer("www.itheima.com");
        //过期时间设置
        jwtBuilder.setExpiration(new Date(System.currentTimeMillis()+15000));

        //自定义载荷
        Map<String,Object> payload = new HashMap<String,Object>();
        payload.put("address","中国");
        payload.put("age",25);
        payload.put("money",100);
        jwtBuilder.addClaims(payload);

        //设置签名的算法以及秘钥
        jwtBuilder.signWith(SignatureAlgorithm.HS256,"itheima");

        //生成了令牌
        String token = jwtBuilder.compact();
        System.out.println(token);
    }


    /***
     * Token解密
     */
    @Test
    public void testParseToken(){
        //String token  = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJObzAwMDEiLCJpYXQiOjE1NjU1ODA1NzUsInN1YiI6IuS4u-mimCIsImlzcyI6Ind3dy5pdGhlaW1hLmNvbSJ9.atNAEUfMSP3IaQOI7ywJKAo5oWDNXVZcBWwBITIXXzE";
        String token  = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJObzAwMDEiLCJpYXQiOjE1NjU1ODA5NTcsInN1YiI6IuS4u-mimCIsImlzcyI6Ind3dy5pdGhlaW1hLmNvbSIsImV4cCI6MTU2NTU4MDk3MiwiYWRkcmVzcyI6IuS4reWbvSIsIm1vbmV5IjoxMDAsImFnZSI6MjV9.hm1-UZm3G_B2b7-Sc_V-09CQ9bs97STfSBsZTmlX_OI";
        Claims claims = Jwts.parser().
                setSigningKey("itheima")   //设置秘钥
                .parseClaimsJws(token).getBody();
        System.out.println(claims.toString());
    }
}
