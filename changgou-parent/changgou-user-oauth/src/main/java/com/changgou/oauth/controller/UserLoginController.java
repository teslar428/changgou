package com.changgou.oauth.controller;

import com.changgou.oauth.service.UserLoginService;
import com.changgou.oauth.util.AuthToken;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.oauth.controller
 ****/
@RestController
@RequestMapping(value = "/user")
@CrossOrigin
public class UserLoginController {

    @Autowired
    private UserLoginService userLoginService;

    //客户端ID
    @Value("${auth.clientId}")
    private String clientId;

    //客户端秘钥
    @Value("${auth.clientSecret}")
    private String clientSecret;

    /****
     * 密码登录
     * 客户端ID(应用程序中)
     * 客户端秘钥(应用程序中)
     * 授权模式(应用程序中)
     * 账号
     * 密码
     */
    @RequestMapping(value = "/login")
    public Result login(String username, String password, HttpServletRequest request, HttpServletResponse response) throws Exception {
        AuthToken authToken = null;
        try {
            //指定授权模式
            String grant_type="password";
            //用户登录实现
            authToken = userLoginService.login(username,password,clientId,clientSecret,grant_type);

            //令牌数据
            //String token = "Bearer "+authToken.getAccessToken();
            String token =authToken.getAccessToken();
            Cookie cookie = new Cookie("Authorization",token);
            cookie.setDomain("changgou.com");  //域名->存在一种跨域
            cookie.setPath("/");
            response.addCookie(cookie);
            return new Result(true, StatusCode.OK,"登录操作",authToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false,StatusCode.REMOTEERROR,"登录失败");
    }

}
