package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.dao.BrandMapper;
import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.dao.SkuMapper;
import com.changgou.goods.dao.SpuMapper;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.math.LongMath;
import entity.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/****
 * @Author:www.itheima.com
 * @Description:Spu业务层接口实现类
 * @Date 黑马畅购商城
 *****/
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SkuMapper skuMapper;

    /***
     * 恢复已删除商品
     * @param spuid
     */
    @Override
    public void restore(Long spuid) {
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(spuid);
        //商品如果没有删除，不需要恢复
        if(spu.getIsDelete().equalsIgnoreCase("0")){
            throw new RuntimeException("该商品不需要恢复！");
        }
        //删除状态->0
        spu.setIsDelete("0");
        //审核状态->0
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /***
     * 逻辑删除
     * @param spuid
     */
    @Override
    public void logicDelete(Long spuid) {
        //查询当前Spu
        Spu spu = spuMapper.selectByPrimaryKey(spuid);
        if(spu.getIsDelete().equalsIgnoreCase("1")){
            throw new RuntimeException("已删除的商品不能再次删除！");
        }
        if(spu.getStatus().equalsIgnoreCase("1")){
            throw new RuntimeException("审核通过的商品不能直接删除！");
        }
        //isDelete=1  删除
        spu.setIsDelete("1");
        //ismartable=0 下架
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /***
     * 批量上架
     * @param ids:需要上架的一组Spu的ID
     */
    @Override
    public void putMany(Long[] ids) {
        //update tb_spu where id in(ids) and is_delete=0 and status=1 and is_markertable=0
        Spu spu = new Spu();
        spu.setIsMarketable("1");   //上架

        //上架需要满足的条件 Example
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));   //构建一个in条件  id in(1,2,6,8,9,45)
        criteria.andEqualTo("isDelete","0");//未删除的商品
        criteria.andEqualTo("status","1"); //审核通过的商品
        criteria.andEqualTo("isMarketable","0"); //已下架的商品才可以上架
        spuMapper.updateByExampleSelective(spu,example);
    }

    /***
     * 商品下架
     * @param spuid
     */
    @Override
    public void pull(Long spuid) {
        //查询商品信息
        Spu spu = spuMapper.selectByPrimaryKey(spuid);

        //判断商品是否已经删除，如果删除了，不能下架，则抛出异常
        if(spu.getIsDelete().equalsIgnoreCase("1")){
            throw new RuntimeException("不能对已删除的商品进行下架！");
        }

        //可以审核,审核通过，下架
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /****
     * 审核商品
     * @param spuid
     */
    @Override
    public void audit(Long spuid) {
        //查询商品信息
        Spu spu = spuMapper.selectByPrimaryKey(spuid);

        //判断商品是否已经删除，如果删除了，不能审核，则抛出异常
        if(spu.getIsDelete().equalsIgnoreCase("1")){
            throw new RuntimeException("不能对已删除的商品进行审核！");
        }

        //可以审核,审核通过，上架
        spu.setStatus("1");
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /***
     * 根据spuid查询Goods
     * @param spuid
     * @return
     */
    @Override
    public Goods findGoodsById(Long spuid) {
        //查询Spu
        Spu spu = spuMapper.selectByPrimaryKey(spuid);
        //查询List<Sku>  select * from tb_sku where spuid=?
        Sku sku = new Sku();
        sku.setSpuId(spuid);
        List<Sku> skuList = skuMapper.select(sku);
        //封装一个Goods
        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skuList);
        return goods;
    }

    /***
     * 增加商品|修改商品
     * 实现保存Goods===>Spu+List<Sku>
     *     1)修改商品->Spu的id值是一个数字
     *     2)增加商品->Spu的id值是一个NULL
     * @param goods
     */
    @Override
    public void saveGoods(Goods goods) {
        //保存Spu
        Spu spu = goods.getSpu();

        //修改商品->Spu的id值是一个数字
        if(spu.getId()!=null){
            //修改Spu
            spuMapper.updateByPrimaryKeySelective(spu);
            //删除之前的List<Sku>  delete from tb_sku where spuid=?
            Sku sku = new Sku();
            sku.setSpuId(spu.getId());
            skuMapper.delete(sku);
        }else{
            //增加商品->Spu的id值是一个NULL
            spu.setId(idWorker.nextId());
            spuMapper.insertSelective(spu);
        }

        //保存List<Sku>
        //取出List<Sku>
        List<Sku> skuList = goods.getSkuList();

        //查询分类名称  3级分类名称
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());

        //查询品牌数据
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());

        //当前时间
        Date date = new Date();

        //循环List<Sku>,逐个保存
        for (Sku sku : skuList) {
            //id
            sku.setId(idWorker.nextId());
            //name=共同名字+规格值 {"尺码":"X","颜色":"红色"}
            String name = spu.getName();
            //防止spec为空
            if(StringUtils.isEmpty(sku.getSpec())){
                sku.setSpec("{}");
            }
            //获取spec，将spec转成Map
            Map<String,String> specMap = JSON.parseObject(sku.getSpec(), Map.class);
            //循环Map，拼接到name后面
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                name+=" "+entry.getValue();
            }
            sku.setName(name);

            //createTime
            sku.setCreateTime(date);
            //updateTime
            sku.setUpdateTime(date);
            //spuId
            sku.setSpuId(spu.getId());
            //categoryId  3级分类
            sku.setCategoryId(spu.getCategory3Id());
            //categoryName
            sku.setCategoryName(category.getName());
            //brandName
            sku.setBrandName(brand.getName());
            //status
            sku.setStatus("1");
            //保存数据
            skuMapper.insertSelective(sku);
        }
    }

    /**
     * Spu条件+分页查询
     * @param spu 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Spu> findPage(Spu spu, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(spu);
        //执行搜索
        return new PageInfo<Spu>(spuMapper.selectByExample(example));
    }

    /**
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Spu> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<Spu>(spuMapper.selectAll());
    }

    /**
     * Spu条件查询
     * @param spu
     * @return
     */
    @Override
    public List<Spu> findList(Spu spu){
        //构建查询条件
        Example example = createExample(spu);
        //根据构建的条件查询数据
        return spuMapper.selectByExample(example);
    }


    /**
     * Spu构建查询对象
     * @param spu
     * @return
     */
    public Example createExample(Spu spu){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(spu!=null){
            // 主键
            if(!StringUtils.isEmpty(spu.getId())){
                    criteria.andEqualTo("id",spu.getId());
            }
            // 货号
            if(!StringUtils.isEmpty(spu.getSn())){
                    criteria.andEqualTo("sn",spu.getSn());
            }
            // SPU名
            if(!StringUtils.isEmpty(spu.getName())){
                    criteria.andLike("name","%"+spu.getName()+"%");
            }
            // 副标题
            if(!StringUtils.isEmpty(spu.getCaption())){
                    criteria.andEqualTo("caption",spu.getCaption());
            }
            // 品牌ID
            if(!StringUtils.isEmpty(spu.getBrandId())){
                    criteria.andEqualTo("brandId",spu.getBrandId());
            }
            // 一级分类
            if(!StringUtils.isEmpty(spu.getCategory1Id())){
                    criteria.andEqualTo("category1Id",spu.getCategory1Id());
            }
            // 二级分类
            if(!StringUtils.isEmpty(spu.getCategory2Id())){
                    criteria.andEqualTo("category2Id",spu.getCategory2Id());
            }
            // 三级分类
            if(!StringUtils.isEmpty(spu.getCategory3Id())){
                    criteria.andEqualTo("category3Id",spu.getCategory3Id());
            }
            // 模板ID
            if(!StringUtils.isEmpty(spu.getTemplateId())){
                    criteria.andEqualTo("templateId",spu.getTemplateId());
            }
            // 运费模板id
            if(!StringUtils.isEmpty(spu.getFreightId())){
                    criteria.andEqualTo("freightId",spu.getFreightId());
            }
            // 图片
            if(!StringUtils.isEmpty(spu.getImage())){
                    criteria.andEqualTo("image",spu.getImage());
            }
            // 图片列表
            if(!StringUtils.isEmpty(spu.getImages())){
                    criteria.andEqualTo("images",spu.getImages());
            }
            // 售后服务
            if(!StringUtils.isEmpty(spu.getSaleService())){
                    criteria.andEqualTo("saleService",spu.getSaleService());
            }
            // 介绍
            if(!StringUtils.isEmpty(spu.getIntroduction())){
                    criteria.andEqualTo("introduction",spu.getIntroduction());
            }
            // 规格列表
            if(!StringUtils.isEmpty(spu.getSpecItems())){
                    criteria.andEqualTo("specItems",spu.getSpecItems());
            }
            // 参数列表
            if(!StringUtils.isEmpty(spu.getParaItems())){
                    criteria.andEqualTo("paraItems",spu.getParaItems());
            }
            // 销量
            if(!StringUtils.isEmpty(spu.getSaleNum())){
                    criteria.andEqualTo("saleNum",spu.getSaleNum());
            }
            // 评论数
            if(!StringUtils.isEmpty(spu.getCommentNum())){
                    criteria.andEqualTo("commentNum",spu.getCommentNum());
            }
            // 是否上架,0已下架，1已上架
            if(!StringUtils.isEmpty(spu.getIsMarketable())){
                    criteria.andEqualTo("isMarketable",spu.getIsMarketable());
            }
            // 是否启用规格
            if(!StringUtils.isEmpty(spu.getIsEnableSpec())){
                    criteria.andEqualTo("isEnableSpec",spu.getIsEnableSpec());
            }
            // 是否删除,0:未删除，1：已删除
            if(!StringUtils.isEmpty(spu.getIsDelete())){
                    criteria.andEqualTo("isDelete",spu.getIsDelete());
            }
            // 审核状态，0：未审核，1：已审核，2：审核不通过
            if(!StringUtils.isEmpty(spu.getStatus())){
                    criteria.andEqualTo("status",spu.getStatus());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Long id){
        Spu spu = spuMapper.selectByPrimaryKey(id);

        //如果商品已经实现了逻辑删除，才能删除该商品
        if(!spu.getIsDelete().equalsIgnoreCase("1")){
            throw new RuntimeException("该商品不能直接删除！");
        }
        //真实删除
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Spu
     * @param spu
     */
    @Override
    public void update(Spu spu){
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 增加Spu
     * @param spu
     */
    @Override
    public void add(Spu spu){
        spuMapper.insert(spu);
    }

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
    @Override
    public Spu findById(Long id){
        return  spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Spu全部数据
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    public static void main(String[] args) {
        System.out.println((long)(Math.random() * 1000));
    }
}
