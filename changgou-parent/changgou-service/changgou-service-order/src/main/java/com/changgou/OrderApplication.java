package com.changgou;

import entity.FeignRequestInerceptor;
import entity.IdWorker;
import feign.RequestInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou
 ****/
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = {"com.changgou.goods.feign","com.changgou.user.feign"})
@MapperScan(basePackages = {"com.changgou.order.dao"})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class,args);
    }


    /***
     * 主键生成
     * IdWorker
     */
    @Bean
    public IdWorker idWorker(){
        return new IdWorker(0,0);
    }

    /***
     * 需要将用户访问的头令牌传递给goods微服务或者其他微服务，所以需要创建拦截器添加头信息
     * FeignRequestInerceptor
     */
    @Bean
    public RequestInterceptor feignRequestInterceptor(){
        return new FeignRequestInerceptor();
    }
}