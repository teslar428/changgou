package com.changgou.content.feign;
import com.changgou.content.pojo.ContentCategory;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:www.itheima.com
 * @Description:
 * @Date www.itheima.com
 *****/
@FeignClient(name="content")
@RequestMapping("/contentCategory")
public interface ContentCategoryFeign {

}