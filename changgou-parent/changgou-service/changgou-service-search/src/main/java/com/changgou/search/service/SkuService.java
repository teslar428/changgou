package com.changgou.search.service;

import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.search.service
 ****/
public interface SkuService {

    /****
     * 导入索引库数据
     */
    void importSku();


    /***
     * 多条件搜索
     */
    Map<String,Object> search(Map<String,String> searchMap);
}
