package com.changgou.goods.dao;
import com.changgou.goods.pojo.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:www.itheima.com
 * @Description:Sku的Dao
 * @Date www.itheima.com
 *****/
public interface SkuMapper extends Mapper<Sku> {

    /****
     * 库存递减
     */
    @Update("UPDATE tb_sku SET num=num-#{num} WHERE id=#{id} AND num>=#{num}")
    int decrCount(@Param("id") Long id,@Param("num") Integer num);
}
