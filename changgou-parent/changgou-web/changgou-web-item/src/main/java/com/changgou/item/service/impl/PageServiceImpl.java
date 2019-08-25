package com.changgou.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.item.service.PageService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.item.service.impl
 ****/
@Service
public class PageServiceImpl implements PageService {

    /***
     * 用它生成静态页
     */
    @Autowired
    private TemplateEngine templateEngine;

    //查询Spu
    @Autowired
    private SpuFeign spuFeign;

    //Spu对应的List<Sku>
    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    //生成静态页存储路径
    @Value("${pagepath}")
    private String pagepath;

    /****
     * 加载所有数据
     */
    public Map<String,Object> buildDataModel(Long spuId){
        Map<String,Object> dataMap = new HashMap<>();
        //1:查询Spu
        Result<Spu> spuResult = spuFeign.findById(spuId);
        Spu spu = spuResult.getData();

        //2:查询分类-3个分类
        Category category1 = categoryFeign.findById(spu.getCategory1Id()).getData();
        Category category2 = categoryFeign.findById(spu.getCategory2Id()).getData();
        Category category3 = categoryFeign.findById(spu.getCategory3Id()).getData();
        dataMap.put("category1",category1);
        dataMap.put("category2",category2);
        dataMap.put("category3",category3);

        //取出Spu的图片
        String[] imageList = spu.getImages().split(",");
        dataMap.put("imageList",imageList);
        //规格集合
        dataMap.put("specificationList",JSON.parseObject(spu.getSpecItems(),Map.class));

        //3:查询List<Sku>
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        Result<List<Sku>> skuResult = skuFeign.findList(sku);
        dataMap.put("skuList",skuResult.getData());
        return dataMap;
    }



    /***
     * 生成静态页
     * @param spuid
     */
    @Override
    public void createHtml(Long spuid) {
        try {
            //创建一个容器对象，用于存储页面所需的变量信息  Context
            Context context = new Context();

            //查询所需数据
            Map<String, Object> dataMap = buildDataModel(spuid);
            context.setVariables(dataMap);

            //获取类路径
            String path = PageServiceImpl.class.getResource("/").getPath()+"/items";

            //判断当前目录是否存在，如果不存在，则创建一个目录
            //File dir = new File(pagepath);
            File dir = new File(path);
            if(!dir.exists()){
                dir.mkdirs();
            }

            //创建一个Writer对象，并指定生成的静态页文件全路径
            //FileWriter fileWriter = new FileWriter(pagepath+"/"+spuid+".html");
            FileWriter fileWriter = new FileWriter(path+"/"+spuid+".html");

            /***
             * 执行生成操作
             * 1:指定模板
             * 2:模板所需的数据模型
             * 3:输出文件对象(文件生成到哪里去)
             */
            templateEngine.process("item",context,fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
