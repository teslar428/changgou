package com.changgou.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.search.feign
 ****/
@FeignClient(value = "search")
@RequestMapping(value = "/search")
public interface SkuFeign {

    /***
     * 搜索实现
     * @GetMapping:
     * @PostMapping:
     * @RequestParam(required = false):用户可以不传任何参数
     */
    @GetMapping
    Map<String,Object> search(@RequestParam(required = false) Map<String,String> searchMap);
}
