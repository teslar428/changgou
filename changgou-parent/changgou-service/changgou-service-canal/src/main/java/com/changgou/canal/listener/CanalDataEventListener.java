package com.changgou.canal.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.content.feign.ContentFeign;
import com.changgou.content.pojo.Content;
import com.xpand.starter.canal.annotation.*;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.canal.listener
 * 实现读取Canal数据
 ****/
@CanalEventListener  //开启Canal数据监听
public class CanalDataEventListener {

    @Autowired
    private ContentFeign contentFeign;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /***
     * 自定义数据监控
     * destination = example:监控的实例指定
     * schema = "":指定监控的数据库
     * table = {"tb_content"}:指定监控的表
     * @ListenPoint(schema = "changgou_content",table = {"tb_content"},):指定监控类型
     */
    @ListenPoint(destination = "example",schema = "changgou_content",table = {"tb_content"},eventType = {CanalEntry.EventType.UPDATE,CanalEntry.EventType.INSERT})
    public void onEventContentUpdate(CanalEntry.EventType eventType,CanalEntry.RowData rowData){
        //获取变更的数据 category_id
        String category_id = getColumn(rowData, "category_id");

        //Feign调用查询对应分类的广告集合
        Result<List<Content>> result = contentFeign.findByCategory(Long.parseLong(category_id));

        //将广告集合压入到Redis缓存
        if(result.getData()!=null){
            stringRedisTemplate.boundValueOps("content_"+category_id).set(JSON.toJSONString(result.getData()));
        }
    }

    /******
     * 增加数据监听
     * eventType:操作类型  增加
     * rowData:当前监控的变化数据
     */
    @InsertListenPoint
    public void onEventInsert(CanalEntry.EventType eventType,CanalEntry.RowData rowData){
        //rowData.getAfterColumnsList():发生变更后的数据
        //rowData.getBeforeColumnsList():数据发生变更前的记录
        System.out.println("============增加监控============");
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            System.out.println(column.getName() + ":" + column.getValue());
        }
    }


    /****
     * 修改数据监听
     */
    @UpdateListenPoint
    public void onEventUpdate(CanalEntry.EventType eventType,CanalEntry.RowData rowData){
        //rowData.getAfterColumnsList():发生变更后的数据
        //rowData.getBeforeColumnsList():数据发生变更前的记录
        System.out.println("============修改监控============");
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            System.out.println(column.getName() + ":" + column.getValue());
        }
    }

    /****
     * 删除数据监听
     */
    @DeleteListenPoint
    public void onEventDelete(CanalEntry.EventType eventType,CanalEntry.RowData rowData){
        //rowData.getAfterColumnsList():发生变更后的数据
        //rowData.getBeforeColumnsList():数据发生变更前的记录
        System.out.println("============删除监控============");
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            System.out.println(column.getName() + ":" + column.getValue());
        }
    }


    /***
     * 自定义数据监控
     * destination = example:监控的实例指定
     * schema = "":指定监控的数据库
     * table = {"tb_content"}:指定监控的表
     * @ListenPoint(schema = "changgou_content",table = {"tb_content"},):指定监控类型
     */
    @ListenPoint(destination = "example",schema = "changgou_content",table = {"tb_content"},eventType = CanalEntry.EventType.DELETE)
    public void onEventCustomUpdate(CanalEntry.EventType eventType,CanalEntry.RowData rowData){
        System.out.println("=====AAA=======删除监控======AAAA======");
        //for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
        //    System.out.println(column.getName() + ":" + column.getValue());
        //}
        String id = getColumn(rowData, "id");
        System.out.println("删除的数据的ID="+id);
    }


    /***
     * 获取某一个列的变化值
     */
    public String getColumn(CanalEntry.RowData rowData,String columnName){
        //获取变化后的值
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            if(column.getName().equalsIgnoreCase(columnName)){
                return column.getValue();
            }
        }

        //获取变化前的值(删除)
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            if(column.getName().equalsIgnoreCase(columnName)){
                return column.getValue();
            }
        }
        return null;
    }


}
