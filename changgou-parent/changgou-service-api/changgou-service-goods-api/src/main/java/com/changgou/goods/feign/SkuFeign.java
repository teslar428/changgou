package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.goods.feign
 ****/
@FeignClient(value = "goods")
@RequestMapping(value = "/sku")
public interface SkuFeign {

    /****
     * 库存递减操作
     * @return
     */
    @PostMapping(value = "/decr/count")
    Result decount();

    /****
     * 根据状态进行搜索
     */
    @GetMapping(value = "/status/{status}")
    Result<List<Sku>> findByStatus(@PathVariable(value = "status")String status);


    /**
     * 根据条件搜索
     * @param sku
     * @return
     */
    @PostMapping(value = "/search" )
    public Result<List<Sku>> findList(@RequestBody(required = false) Sku sku);


    /***
     * 根据ID查询Sku数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Sku> findById(@PathVariable Long id);
}
