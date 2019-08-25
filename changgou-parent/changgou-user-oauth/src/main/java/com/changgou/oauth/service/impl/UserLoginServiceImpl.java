package com.changgou.oauth.service.impl;

import com.changgou.oauth.service.UserLoginService;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.oauth.service.impl
 ****/
@Service
public class UserLoginServiceImpl implements UserLoginService {


    @Autowired
    private RestTemplate restTemplate;

    //通过负载均衡实现调用登录
    @Autowired
    private LoadBalancerClient loadBalancerClient;

    /***
     * 密码登录
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @param grant_type
     */
    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret, String grant_type)throws Exception {
        /*****
         * 实现登录操作
         * 1:确定请求地址
         * 2:账号、密码、授权模式(参数提交)
         * 3:客户端ID、秘钥(头文件提交)  Authorization  Basic Base64(clientId:clientSecret)
         */

        //loadBalancerClient
        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");

        //登录地址
        //String url = "http://localhost:9001/oauth/token";
        String url = serviceInstance.getUri().toString()+ "/oauth/token";

        //封装提交数据/提交Header
        MultiValueMap parameters = new LinkedMultiValueMap();
        parameters.add("username",username);
        parameters.add("password",password);
        parameters.add("grant_type",grant_type);

        //头文件封装
        MultiValueMap headers = new LinkedMultiValueMap();
        headers.add("Authorization","Basic "+new String(Base64.getEncoder().encode((clientId+":"+clientSecret).getBytes()),"UTF-8"));

        //HttpEntity对象封装
        HttpEntity httpEntity = new HttpEntity(parameters,headers);

        /***
         * 提交请求
         * 1:请求地址
         * 2:请求提交方式
         * 3:请求提交数据(headers[请求头]/body[请求体])
         * 4:返回数据需要转换的类型
         */
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);

        //获取响应数据
        Map<String,String> resultMap = response.getBody();
        //将用户信息封装成一个令牌对象信息
        AuthToken authToken = new AuthToken();
        authToken.setJti(resultMap.get("jti"));
        authToken.setRefreshToken(resultMap.get("refresh_token"));
        authToken.setAccessToken(resultMap.get("access_token"));

        return authToken;
    }


}
