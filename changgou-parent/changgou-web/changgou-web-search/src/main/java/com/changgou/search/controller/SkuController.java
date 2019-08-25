package com.changgou.search.controller;

import com.changgou.search.feign.SkuFeign;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.search.controller
 ****/
@Controller
@RequestMapping(value = "/search")
public class SkuController {

    @Autowired
    private SkuFeign skuFeign;

    /****
     * 搜索案例
     */
    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false)Map<String,String> searchMap, Model model){
        //调用搜索微服务实现搜索
        Map<String, Object> result = skuFeign.search(searchMap);
        model.addAttribute("result",result);   //rows

        /***
         * 1：总记录数
         * 2:当前页
         * 3:每页显示的条数
         */
        Page page = new Page(
                Long.parseLong(result.get("total").toString()),
                Integer.parseInt(result.get("pageNum").toString()),
                Integer.parseInt(result.get("pageSize").toString()));
        model.addAttribute("page",page);

        //获取URL
        String url = url(searchMap);
        model.addAttribute("url",url);

        //搜索条件存储
        model.addAttribute("searchMap",searchMap);
        return "search";
    }

    /****
     * 拼接上次请求的URL路径
     * @param searchMap
     * @return
     */
    public String url(Map<String,String> searchMap){
        //用户没有搜索任何条件的地址
        String url="/search/list";

        //条件对象则不为空
        if(searchMap!=null){
            //url？
            url+="?";

            //循环searchMap组装搜索条件   brand=华为      category=手机         spec_网络=移动4G
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key=entry.getKey();      //brand
                String value = entry.getValue();//华为

                //参数如果是pageNum,则不拼接
                if(key.equalsIgnoreCase("pageNum")){
                    continue;
                }

                //url拼接
                url+=key+"="+value+"&";
            }
            //去掉最有一个&
            url=url.substring(0,url.length()-1);
        }
        return url;
    }

}
