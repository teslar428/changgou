package com.changgou.goods.controller;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.changgou.goods.service.SpuService;
import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:www.itheima.com
 * @Description:
 * @Date www.itheima.com
 *****/

@RestController
@RequestMapping("/spu")
@CrossOrigin
public class SpuController {

    @Autowired
    private SpuService spuService;

    /***
     * 商品找回操作
     */
    @PutMapping(value ="/restore/{id}")
    public Result restore(@PathVariable(value = "id")Long spuId){
        spuService.restore(spuId);
        return new Result(true,StatusCode.OK,"商品找回成功！");
    }

    /***
     * 逻辑删除操作
     */
    @DeleteMapping(value ="/logic/delete/{id}")
    public Result logicDelete(@PathVariable(value = "id")Long spuId){
        spuService.logicDelete(spuId);
        return new Result(true,StatusCode.OK,"逻辑删除成功！");
    }


    /***
     * 批量上架
     */
    @PutMapping(value ="/put/many")
    public Result putMany(@RequestBody Long[] ids){
        spuService.putMany(ids);
        return new Result(true,StatusCode.OK,"批量上架成功！");
    }

    /***
     * pull下架操作
     */
    @PutMapping(value ="/pull/{id}")
    public Result pull(@PathVariable(value = "id")Long spuId){
        spuService.pull(spuId);
        return new Result(true,StatusCode.OK,"下架成功！");
    }

    /****
     * 商品审核
     */
    @PutMapping(value ="/audit/{id}")
    public Result audit(@PathVariable(value = "id")Long spuId){
        spuService.audit(spuId);
        return new Result(true,StatusCode.OK,"审核成功！");
    }

    /***
     * 根据SPUId查询Goods
     */
    @GetMapping(value = "/goods/{id}")
    public Result<Goods> findByGoodsId(@PathVariable(value = "id")Long spuid){
        Goods goods = spuService.findGoodsById(spuid);
        return new Result<Goods>(true,StatusCode.OK,"查询Goods成功！",goods);
    }

    /***
     * 实现保存Goods===>Spu+List<Sku>
     * @param goods
     * @return
     */
    @PostMapping(value = "/save")
    public Result saveGoods(@RequestBody Goods goods){
        //调用Service实现增加
        spuService.saveGoods(goods);
        return new Result(true,StatusCode.OK,"保存Goods成功！",goods);
    }

    /***
     * Spu分页条件搜索实现
     * @param spu
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@RequestBody(required = false)  Spu spu, @PathVariable  int page, @PathVariable  int size){
        //调用SpuService实现分页条件查询Spu
        PageInfo<Spu> pageInfo = spuService.findPage(spu, page, size);
        return new Result(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * Spu分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@PathVariable  int page, @PathVariable  int size){
        //调用SpuService实现分页查询Spu
        PageInfo<Spu> pageInfo = spuService.findPage(page, size);
        return new Result<PageInfo>(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * 多条件搜索品牌数据
     * @param spu
     * @return
     */
    @PostMapping(value = "/search" )
    public Result<List<Spu>> findList(@RequestBody(required = false)  Spu spu){
        //调用SpuService实现条件查询Spu
        List<Spu> list = spuService.findList(spu);
        return new Result<List<Spu>>(true,StatusCode.OK,"查询成功",list);
    }

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable Long id){
        //调用SpuService实现根据主键删除
        spuService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 修改Spu数据
     * @param spu
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody  Spu spu,@PathVariable Long id){
        //设置主键值
        spu.setId(id);
        //调用SpuService实现修改Spu
        spuService.update(spu);
        return new Result(true,StatusCode.OK,"修改成功");
    }

    /***
     * 新增Spu数据
     * @param spu
     * @return
     */
    @PostMapping
    public Result add(@RequestBody   Spu spu){
        //调用SpuService实现添加Spu
        spuService.add(spu);
        return new Result(true,StatusCode.OK,"添加成功");
    }

    /***
     * 根据ID查询Spu数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Spu> findById(@PathVariable Long id){
        //调用SpuService实现根据主键查询Spu
        Spu spu = spuService.findById(id);
        return new Result<Spu>(true,StatusCode.OK,"查询成功",spu);
    }

    /***
     * 查询Spu全部数据
     * @return
     */
    @GetMapping
    public Result<List<Spu>> findAll(){
        //调用SpuService实现查询所有Spu
        List<Spu> list = spuService.findAll();
        return new Result<List<Spu>>(true, StatusCode.OK,"查询成功",list) ;
    }
}
