package com.changgou.goods.feign;

import com.changgou.goods.pojo.Category;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.goods.feign
 ****/
@FeignClient(value = "goods")
@RequestMapping(value = "/category")
public interface CategoryFeign {

    /**
     * 获取分类的对象信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Category> findById(@PathVariable(name = "id") Integer id);

}
