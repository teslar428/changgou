package com.changgou.goods.service;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.PageInfo;

import java.util.List;

/****
 * @Author:www.itheima.com
 * @Description:Spu业务层接口
 * @Date 黑马畅购商城
 *****/
public interface SpuService {

    /***
     * 恢复已删除商品
     */
    void restore(Long spuid);

    /***
     * 逻辑删除
     */
    void logicDelete(Long spuid);

    /***
     * 批量上架
     * @param ids:需要上架的一组Spu的ID
     */
    void putMany(Long[] ids);

    /***
     * 商品下架
     */
    void pull(Long spuid);

    /****
     * 商品审核
     * @param spuid
     */
    void audit(Long spuid);

    /***
     * 根据spuid查询goods数据
     * @param spuid
     * @return Goods
     */
    Goods findGoodsById(Long spuid);


    /***
     * 实现保存Goods===>Spu+List<Sku>
     * @param goods
     */
    void saveGoods(Goods goods);

    /***
     * Spu多条件分页查询
     * @param spu
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(Spu spu, int page, int size);

    /***
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(int page, int size);

    /***
     * Spu多条件搜索方法
     * @param spu
     * @return
     */
    List<Spu> findList(Spu spu);

    /***
     * 删除Spu
     * @param id
     */
    void delete(Long id);

    /***
     * 修改Spu数据
     * @param spu
     */
    void update(Spu spu);

    /***
     * 新增Spu
     * @param spu
     */
    void add(Spu spu);

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
     Spu findById(Long id);

    /***
     * 查询所有Spu
     * @return
     */
    List<Spu> findAll();
}
