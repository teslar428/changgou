package com.changgou.item.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.item.config
 ****/
@Configuration
public class EnableMvcConfig implements WebMvcConfigurer {

    /****
     * 静态资源过滤
     * mapping:请求路径的映射
     * location:本地查找路径
     * <mvc:resources mapping="" location="" />
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/items/**").          //mapping:请求路径的映射
                addResourceLocations("classpath:/items/");    //location:本地查找路径
    }
}
