package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

/*****
 * @Author: 黑马训练营-畅购商城 www.itheima.com
 * @Date:  黑马训练营-畅购商城 www.itheima.com7 16:23
 * @Description: com.changgou.oauth.service
 ****/
public interface AuthService {

    /***
     * 授权认证方法
     */
    AuthToken login(String username, String password, String clientId, String clientSecret);
}
