package com.changgou.oauth.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.oauth.config
 * 拦截器:调用Feign之前，执行拦截器，生成令牌，并且将令牌存入头文件中，此时Feign调用会一起将令牌传过去
 ****/
@Configuration
public class FeignOauth2RequestInterceptor implements RequestInterceptor {

    /***
     * 拦截器:调用Feign之前，执行拦截器，生成令牌，并且将令牌存入头文件中，此时Feign调用会一起将令牌传过去
     * 有可能存在别的头信息
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {
        //生成令牌
        String adminToken = JwtToken.adminJwt();

        //将令牌添加到头文件中
        template.header("Authorization","Bearer "+adminToken);
    }
}
