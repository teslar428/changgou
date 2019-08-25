package com.changgou.search.dao;

import com.changgou.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.search.dao
 * 集成SpringData Elasticsearch
 ****/
public interface SkuEsMapper extends ElasticsearchRepository<SkuInfo,Long>{
}
