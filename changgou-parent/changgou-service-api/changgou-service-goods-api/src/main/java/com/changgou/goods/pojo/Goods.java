package com.changgou.goods.pojo;

import java.util.List;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.goods.pojo
 ****/
public class Goods {

    //Spu
    private Spu spu;

    //List<Sku>
    private List<Sku> skuList;

    public Spu getSpu() {
        return spu;
    }

    public void setSpu(Spu spu) {
        this.spu = spu;
    }

    public List<Sku> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<Sku> skuList) {
        this.skuList = skuList;
    }
}
