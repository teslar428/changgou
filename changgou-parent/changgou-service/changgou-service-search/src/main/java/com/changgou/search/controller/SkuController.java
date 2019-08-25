package com.changgou.search.controller;

import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.search.controller
 ****/
@RestController
@RequestMapping(value = "/search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    /****
     * 导入到ES索引库调用
     */
    @GetMapping("/import")
    public Result importSku(){
        //调用Service实现导入操作
        skuService.importSku();
        return new Result(true, StatusCode.OK,"导入数据到ES中成功！");
    }


    /***
     * 搜索实现
     * @GetMapping:
     * @PostMapping:
     * @RequestParam(required = false):用户可以不传任何参数
     */
    @GetMapping
    public Map<String,Object> search(@RequestParam(required = false) Map<String,String> searchMap){
        //调用搜索
        Map<String, Object> resultMap = skuService.search(searchMap);
        return resultMap;
    }


}
