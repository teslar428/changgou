package com.changgou.user.feign;

import com.changgou.user.pojo.Address;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.user.feign
 ****/
@FeignClient(value = "user")
@RequestMapping(value = "/address")
public interface AddressFeign {

    /***
     * 根据用户登录名查询用户收件列表
     */
    @GetMapping(value = "/user/list")
    Result<List<Address>> list();
}
