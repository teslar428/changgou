package com.changgou.filter;

import com.changgou.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.filter
 * 全局过滤器
 ****/
//@Component       // Jdk Proxy
@Configuration   //配置类 ->给对象创建实例->CglibProxy
public class AuthorizeFilter implements GlobalFilter, Ordered {

    /****
     * 令牌参数名字
     * 1:头文件中
     * 2:参数中
     * 3:Cookie
     */
    public static final String AUTHORIZE_TOKEN = "Authorization";

    //登录地址
    public static String LOGIN_URL="http://localhost:9001/oauth/login";

    /****
     * 全局拦截操作
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //Request
        ServerHttpRequest request = exchange.getRequest();
        //Response
        ServerHttpResponse response = exchange.getResponse();

        //获取用户请求的地址
        String uri = request.getURI().getPath();

        //鉴权->简单的鉴权[用户登录无需鉴权]   /api/user/loigin
        if(!URLFilter.hasAuthorize(uri)){
            return chain.filter(exchange);
        }

        //其他地址，需要鉴权->1)从请求头中获取Authorization令牌数据
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        boolean hasToken = true;    //true:头中有令牌  false：头中无令牌

        //其他地址，需要鉴权->2)请求头中没有令牌数据，则从参数中回去Authorization令牌数据
        if(StringUtils.isEmpty(token)){
            hasToken=false;// 头中无令牌
            token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
        }

        //其他地址，需要鉴权->3)请求参数中没有Authorization令牌数据，则从Cookie中获取
        if(StringUtils.isEmpty(token)){
            hasToken=false;// 头中无令牌
            HttpCookie cookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if(cookie!=null){
                token = cookie.getValue();
                //Cookie中没有:Bearer
                if(!StringUtils.isEmpty(token)){
                    token="Bearer "+token;
                }
            }
        }
        //如果以上获取途径都无法获取令牌，则拒绝访问
        if(token==null){
            //拦截
            //response.setStatusCode(HttpStatus.UNAUTHORIZED);    //状态码
            //return response.setComplete();    //结束流程

            //必须添加状态码
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //跳转到登录页  http://localhost:9001/oauth/login?FROM=uri
            //String url = LOGIN_URL+"?FROM="+request.getURI().toString();
            //跳转到指定地址
            //response.getHeaders().add("Location",url);
            return response.setComplete();
        }

        //如果有，并且能解析，则放行
        try {
            //解析令牌   公钥私钥
            //Claims claims = JwtUtil.parseJWT(token);

            //请求头中无令牌，则将令牌存入头中
            if(!hasToken){
                if(!token.startsWith("Bearer") && !token.startsWith("bearer") && !token.startsWith("BEARER")){
                    token="Bearer "+token;
                }
                //手动添加一些头信息
                request.mutate().header(AUTHORIZE_TOKEN,token);
            }
        } catch (Exception e) {
            e.printStackTrace();

            //拦截
            response.setStatusCode(HttpStatus.UNAUTHORIZED);    //状态码
            return response.setComplete();    //结束流程
        }

        //放行
        return chain.filter(exchange);
    }

    /***
     * 过滤器的执行顺序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
