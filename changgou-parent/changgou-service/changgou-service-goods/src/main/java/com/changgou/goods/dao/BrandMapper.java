package com.changgou.goods.dao;
import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/****
 * @Author:www.itheima.com
 * @Description:Brand的Dao
 * @Date www.itheima.com
 *****/
public interface BrandMapper extends Mapper<Brand> {

    /***
     * 根据分类ID查询分类对应的品牌集合
     * @param categoryid
     * @return
     */
    @Select("SELECT tb.* FROM tb_category_brand tcb,tb_brand tb WHERE tb.id=tcb.brand_id AND tcb.category_id=#{categoryid}")
    List<Brand> findByCategory(Integer categoryid);
}
