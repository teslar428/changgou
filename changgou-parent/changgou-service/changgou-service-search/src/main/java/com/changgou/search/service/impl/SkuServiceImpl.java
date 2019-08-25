package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.search.service.impl
 ****/
@Service
public class SkuServiceImpl implements SkuService {


    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    /***
     * 导入数据到ES索引库
     */
    @Override
    public void importSku() {
        //Feign调用，查询Lsit<Sku>集合
        Result<List<Sku>> result = skuFeign.findByStatus("1");

        //将List<Sku>转换成List<SkuInfo>
        List<Sku> skuList = result.getData();
        List<SkuInfo> skuInfoList =JSON.parseArray(JSON.toJSONString(skuList),SkuInfo.class);//List<Sku>->JSON(JSON.toJSONString(skuList))->List<SkuInfo>JSON.parseArray(json)

        //循环将规格转成Map 对象，用于动态生成规格域
        for (SkuInfo skuInfo : skuInfoList) {
            //获取spec
            String spec = skuInfo.getSpec();
            //将spec转成Map
            Map<String,Object> specMap = JSON.parseObject(spec,Map.class);
            //将map赋值给specMap属性
            skuInfo.setSpecMap(specMap);
        }

        //调用Dao将List<SkuInfo>导入到ES索引库
        skuEsMapper.saveAll(skuInfoList);
    }


    /***
     * 用来实现对ES索引库的操作
     */
    @Autowired
    private ElasticsearchTemplate esTemplate;

    /***
     * 搜索
     * @param searchMap
     *          key:keywrods : 关键词
     *              category : 分类
     *              brand    : 品牌
     *              spec_网络制式 : spec_ 表示规格搜索   value : 电信2G
     *              price : 0-500
     *                      500-100
     *                      1000-1500
     *                      1500-2000
     *                      2000-3000
     *                      3000
     *             pageNum : 5  分页参数，第几页
     *             sortFeild : 排序的域    price   updatetime
     *             sortRule : 排序的规则   ASC/DESC
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        /***
         * 1.搜索条件的封装操作
         */
        NativeSearchQueryBuilder builder = buildBasicQuery(searchMap);

        /****
         * 2.数据搜索实现->在这里实现高亮
         */
        Map<String,Object> resultMap = searchList(builder);

        /***
         * 3.分类[展示的分类条件]搜索实现
         */
        if(searchMap==null || StringUtils.isEmpty(searchMap.get("category"))){
            List<String> categoryList = searchCategoryList(builder);
            resultMap.put("categoryList",categoryList);// 分类分组查询
        }


        /***
         * 4.品牌分组搜索
         */
        if(searchMap==null || StringUtils.isEmpty(searchMap.get("brand"))){
            List<String> brandList = searchBrandList(builder);
            resultMap.put("brandList",brandList);
        }


        /***
         * 5.规格查询
         */
        Map<String, Set<String>> specList = searchSpecList(builder);
        resultMap.put("specList",specList);

        //调用优化代码查询
        //Map<String, Object> groupResultMap = groupList(builder);
        //resultMap.putAll(groupResultMap);
        return resultMap;
    }


    /****
     * 规格条件搜索
     * @param builder
     * @return
     */
    public Map<String,Set<String>> searchSpecList(NativeSearchQueryBuilder builder){
        /****
         * 根据规格(spec)分组搜索
         * 1:给分组搜索取一个别名
         * 2:指定分组的域
         */
        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(100000));//添加聚合操作

        //执行搜索->分组搜索
        AggregatedPage<SkuInfo> specPage = esTemplate.queryForPage(builder.build(), SkuInfo.class);
        //获取聚合搜索的结果集
        Aggregations aggregations = specPage.getAggregations();
        //获取指定分组的数据  根据别名获取,List<String>
        StringTerms stringTerms = aggregations.get("skuSpec");

        //循环所有规格分组数据，并且将它存入到List<String>集合中
        List<String> specList = new ArrayList<String>();

        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            //循环取数据
            String spec = bucket.getKeyAsString(); //规格值
            specList.add(spec);
        }

        //过滤汇总规格数据，组装的结果Map<String,Set<String>
        Map<String, Set<String>> resultSpecMap = putAllSpec(specList);
        return resultSpecMap;
    }


    /****
     * 过滤汇总规格数据，组装的结果List<Map<String,Set<String>>
     * @param specList
     */
    public Map<String, Set<String>> putAllSpec(List<String> specList){
        //定义一个返回的集合对象 Map<String,Set<String>
        Map<String,Set<String>> resultSpecMap = new HashMap<String,Set<String>>();
        //Map-> 电视音响效果  ["小影院"]
        //循环每个spec
        for (String spec : specList) {
            //将每个spec转换成Map
            Map<String,String> specMap = JSON.parseObject(spec, Map.class);

            //循环specMap
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                String key = entry.getKey();        //规格名字  电视音响效果
                String value = entry.getValue();    //规格值    立体声

                //将当前规格存入到resultSpecMap中
                //spectSet：从当前resultSpecMap中获取当前key对应的结果集
                Set<String> specSet = resultSpecMap.get(key);
                if(specSet==null){
                    specSet = new HashSet<String>();
                }
                specSet.add(value);
                //key  电视音响效果  ["立体声"]
                //并且将Map汇总起来
                resultSpecMap.put(key,specSet);
            }
        }
        return resultSpecMap;
    }


    /****
     * 品牌条件搜索
     * @param builder
     * @return
     */
    public List<String> searchBrandList(NativeSearchQueryBuilder builder){
        /****
         * 根据品牌分组搜索
         * 1:给分组搜索取一个别名
         * 2:指定分组的域
         */
        builder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));//添加聚合操作

        //执行搜索->分组搜索
        AggregatedPage<SkuInfo> brandPage = esTemplate.queryForPage(builder.build(), SkuInfo.class);
        //获取聚合搜索的结果集
        Aggregations aggregations = brandPage.getAggregations();
        //获取指定分组的数据  根据别名获取,List<String>
        StringTerms stringTerms = aggregations.get("skuBrand");

        //循环所有品牌分组数据，并且将它存入到List<String>集合中
        List<String> brandList = new ArrayList<String>();

        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            //循环取数据
            String brandName = bucket.getKeyAsString(); //品牌的名字
            brandList.add(brandName);
        }
        return brandList;
    }


    /****
     * 分类条件搜索
     * @param builder
     * @return
     */
    public List<String> searchCategoryList(NativeSearchQueryBuilder builder){
        /***
         * 查询分类个数->把分类作为分组对象
         * AggregationBuilder：有一个构件工具对象AggregationBuilders
         * AggregationBuilders.terms("分组域取个别名").field("要分组的域")
         * 取别名作用：根据别名获取分组数据
         */
        builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));//添加聚合操作

        //执行搜索->分组搜索
        AggregatedPage<SkuInfo> cagegoryPage = esTemplate.queryForPage(builder.build(), SkuInfo.class);
        Aggregations aggregations = cagegoryPage.getAggregations();//获取聚合搜索的结果集
        //获取指定分组的数据  根据别名获取,List<String>
        StringTerms stringTerms = aggregations.get("skuCategory");
        //循环所有分类分组数据，并且将它存入到List<String>集合中
        List<String> categoryList = new ArrayList<String>();

        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            //循环取数据
            String categoryName = bucket.getKeyAsString(); //分类的名字
            categoryList.add(categoryName);
        }
        return categoryList;
    }

    /****
     * 搜索方法
     */
    public Map searchList(NativeSearchQueryBuilder builder){
        /****
         * 1)指定高亮域
         * 2)指定关键词的前缀标签和后缀标签
         * 3)开启高亮
         */
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");//指定高亮域
        field.preTags("<em style='color:red;'>");  //前缀标签
        field.postTags("</em>");    //后缀标签
        field.fragmentSize(100);    //高亮数据的长度
        //开启高亮
        builder.withHighlightFields(field);

        /***
         * 执行搜索
         * 1:搜索条件封装对象
         * 2:搜索的结果需要转换的JavaBean的字节码对象
         * 3:搜索结果的映射封装
         */
        //AggregatedPage<SkuInfo> skuinfoPage = esTemplate.queryForPage(builder.build(), SkuInfo.class);

        //获取searchQuery对象
        NativeSearchQuery searchQuery = builder.build();

        /***
         * 高亮搜索
         */
        AggregatedPage<SkuInfo> skuinfoPage = esTemplate.queryForPage(searchQuery, SkuInfo.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                //封装一个List存储结果集
                List<T> list = new ArrayList<T>();

                //获取所有数据,循环所有数据
                for (SearchHit hit : response.getHits()) {
                    //获取当前记录，非高亮
                    String json = hit.getSourceAsString();//获取对象的JSON字符串
                    //System.out.println("JSON:"+json);
                    //将json字符串转成SkuInfo对象
                    SkuInfo skuInfo = JSON.parseObject(json,SkuInfo.class);

                    //获取高亮数据  highlightFields:所有的高亮域数据
                    Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                    //程序只开启了name的高亮域，只用获取name的高亮数据
                    if(highlightFields!=null){
                        //获取name的高亮数据
                        HighlightField highlightField = highlightFields.get("name");
                        if(highlightField!=null && highlightField.getFragments()!=null && highlightField.getFragments().length>0){
                            //获取高亮碎片  高亮数据
                            String strhl = highlightField.getFragments()[0].toString();
                            //非高亮数据替换成高亮数据
                            skuInfo.setName(strhl);
                        }
                    }
                    //将skuInfo存储到List集合中
                    list.add((T) skuInfo);
                }

                /***
                 * 返回对象封装
                 * 1:结果集List<SkuInfo>
                 * 2:分页对象
                 * 3:总记录数
                 */
                return new AggregatedPageImpl<T>(list,pageable,response.getHits().getTotalHits());
            }
        });

        //解析搜索结果
        Map<String, Object> resultMap = new HashMap<String,Object>();
        resultMap.put("rows",skuinfoPage.getContent());         // 集合数据
        resultMap.put("total",skuinfoPage.getTotalElements());  // 总记录数
        resultMap.put("totalPages",skuinfoPage.getTotalPages());// 总页数
        int pageNum = searchQuery.getPageable().getPageNumber()+1;  //查询当前页  从0开始
        int pageSize = searchQuery.getPageable().getPageSize();      //每页显示多少条
        resultMap.put("pageNum",pageNum);
        resultMap.put("pageSize",pageSize);
        return resultMap;
    }

    /****
     * 搜索条件的封装操作
     */
    public NativeSearchQueryBuilder buildBasicQuery(Map<String,String> searchMap){
        /***
         * 解析搜索条件
         * NativeSearchQuery : ES中每个Query对象都对应着一个QueryBuilder构造对象
         */
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        //BoolQuery,组合多个条件查询:must,must_not,should  and / not in / or
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //条件匹配
        if(searchMap!=null){
            //关键词搜索,如果关键词不为空
            if(!StringUtils.isEmpty(searchMap.get("keywords"))){
                //则将关键词作为搜索条件，搜索name域
                //builder.withQuery(QueryBuilders.matchQuery("name",searchMap.get("keywords")));

                //关键词搜索必须满足
                boolQueryBuilder.must(QueryBuilders.matchQuery("name",searchMap.get("keywords")));
            }

            //分类搜索
            if(!StringUtils.isEmpty(searchMap.get("category"))){
                //分类搜索必须满足
                boolQueryBuilder.must(QueryBuilders.matchQuery("categoryName",searchMap.get("category")));
            }

            //品牌搜索
            if(!StringUtils.isEmpty(searchMap.get("brand"))){
                //品牌搜索必须满足
                boolQueryBuilder.must(QueryBuilders.matchQuery("brandName",searchMap.get("brand")));
            }

            //规格搜索
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                //key = spec_网络制式
                String key = entry.getKey();
                //value  :  移动4G
                String value = entry.getValue();
                //规格查询
                if(key.startsWith("spec_")){
                    //规格搜索必须满足     specMap.规格名字.keyword
                    boolQueryBuilder.must(QueryBuilders.matchQuery("specMap."+key.substring(5)+".keyword",value));
                }
            }

            //价格封装
            String price = searchMap.get("price");
            if(!StringUtils.isEmpty(price)){
                //元和以上去掉
                price = price.replace("元","").replace("以上","");

                //select * from tb_sku where prices[0]<price and price<=prices[1]
                //根据中划线分割   String[] prices
                String[] prices = price.split("-");
                //如果prices>=1      prices[0]<price(域)
                if(prices.length>=1){
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(prices[0])));
                }

                //如果prices==2      (域)price<=prices[1]
                if(prices.length==2){
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.parseInt(prices[1])));
                }
            }

            //排序实现
            String sortField = searchMap.get("sortField");  //排序的域
            String sortRule = searchMap.get("sortRule");   //排序规则

            if(!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRule)){
                /***
                 * SortBuilders.fieldSort("排序的域")
                 * SortOrder.valueOf("ASC/DESC")
                 */
                builder.withSort(SortBuilders.fieldSort(sortField).order(SortOrder.valueOf(sortRule)));
            }
        }

        //默认第1页
        int pageNum=pageConverter(searchMap); //当前页
        int size = 20;  //每页显示的条数
        //配置分页  pageNum从0开始
        builder.withPageable(PageRequest.of(pageNum-1,size));

        //添加查询构建对象
        builder.withQuery(boolQueryBuilder);
        return builder;
    }


    /****
     * 分页参数获取
     * @param searchMap
     * @return
     */
    public int pageConverter(Map<String,String> searchMap){
        try {
            if(searchMap!=null){
                //获取分页数据
                String pageNum = searchMap.get("pageNum");
                return Integer.parseInt(pageNum);
            }
        } catch (Exception e) {
        }
        return 1;
    }






    /*************************************优化代码*****************************************/
    /****
     * 分类条件搜索
     * @param builder
     * @return
     */
    public Map<String,Object> groupList(NativeSearchQueryBuilder builder){
        /***
         * 品牌|分类|规格分组查询
         * AggregationBuilder：有一个构件工具对象AggregationBuilders
         * AggregationBuilders.terms("分组域取个别名").field("要分组的域")
         * 取别名作用：根据别名获取分组数据
         */
        builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName").size(100));   //添加聚合操作(分类)
        builder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName").size(100));         //添加聚合操作(品牌)
        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(100000));    //添加规格聚合操作

        //执行搜索->分组搜索
        AggregatedPage<SkuInfo> cagegoryPage = esTemplate.queryForPage(builder.build(), SkuInfo.class);
        Aggregations aggregations = cagegoryPage.getAggregations();//获取聚合搜索的结果集

        //获取分类分组结果集
        List<String> categoryList = getGroupList(aggregations,"skuCategory");

        //获取分类分组结果集
        List<String> brandList = getGroupList(aggregations,"skuBrand");

        //获取规格分组结果集
        List<String> specList = getGroupList(aggregations,"skuSpec");
        //处理规格数据
        Map<String, Set<String>> resultSpecMap = putAllSpec(specList);

        //Map存储所有结果集数据
        Map<String,Object> resultMap = new HashMap<String,Object>();
        resultMap.put("categoryList",categoryList);
        resultMap.put("brandList",brandList);
        resultMap.put("specList",resultSpecMap);
        return resultMap;
    }

    /***
     * 获取分组结果查询
     * @param aggregations
     * @param groupName
     * @return
     */
    public List<String> getGroupList(Aggregations aggregations,String groupName){
        //获取指定分组的数据  根据别名获取,List<String>
        StringTerms stringTerms = aggregations.get(groupName);
        //循环所有分类分组数据，并且将它存入到List<String>集合中
        List<String> list = new ArrayList<String>();

        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            //循环取数据
            String name = bucket.getKeyAsString(); //结果数据
            list.add(name);
        }
        return list;
    }

}
