package com.changgou.user.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.user.pojo.User;
import com.changgou.user.service.UserService;
import com.github.pagehelper.PageInfo;
import entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/****
 * @Author:www.itheima.com
 * @Description:
 * @Date www.itheima.com
 *****/

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    /***
     * 添加用户积分
     */
    @GetMapping(value = "/points/add")
    public Result addPoints(Integer points){
        //获取用户名
        String username = TokenDecode.getUserInfo().get("username");
        //添加积分
        userService.addPoints(username,points);
        return new Result(true,StatusCode.OK,"增加积分成功！");
    }


    /***
     * 根据账号密码登录
     */
    @GetMapping(value = "/login")
    public Result login(@RequestParam(value = "username")String username,
                        @RequestParam(value = "password")String password,
                        HttpServletRequest request,
                        HttpServletResponse response){
        //实现登录操作
        User user = userService.findById(username);

        /***
         * 校验用户的密码
         * BCrypt.checkpw(password,user.getPassword())
         */
        if(user!=null && BCrypt.checkpw(password,user.getPassword())){
            //把用户信息转成JSON,封装成Map->添加载荷
            Map<String,Object> dataMap = new HashMap<String,Object>();
            dataMap.put("role","USER");
            dataMap.put("status","SUCCESS");
            dataMap.put("username","www.itheima.com");
            dataMap.put("userinfo",user);

            //使用工具类创建令牌
            String token = JwtUtil.createJWT(UUID.randomUUID().toString(), JSON.toJSONString(dataMap), null);
            //把令牌存储到Header中
            response.addHeader("Authorization",token);
            //把令牌存入到Cookie中
            Cookie cookie = new Cookie("Authorization",token);
            response.addCookie(cookie);
            return new Result(true,StatusCode.OK,"登录成功！",token);
        }

        //登录失败
        return new Result(false,StatusCode.LOGINERROR,"密码或账号错误！");
    }

    /***
     * User分页条件搜索实现
     * @param user
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@RequestBody(required = false)  User user, @PathVariable  int page, @PathVariable  int size){
        //调用UserService实现分页条件查询User
        PageInfo<User> pageInfo = userService.findPage(user, page, size);
        return new Result(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * User分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@PathVariable  int page, @PathVariable  int size){
        //调用UserService实现分页查询User
        PageInfo<User> pageInfo = userService.findPage(page, size);
        return new Result<PageInfo>(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * 多条件搜索品牌数据
     * @param user
     * @return
     */
    @PostMapping(value = "/search" )
    public Result<List<User>> findList(@RequestBody(required = false)  User user){
        //调用UserService实现条件查询User
        List<User> list = userService.findList(user);
        return new Result<List<User>>(true,StatusCode.OK,"查询成功",list);
    }

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable String id){
        //调用UserService实现根据主键删除
        userService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 修改User数据
     * @param user
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody  User user,@PathVariable String id){
        //设置主键值
        user.setUsername(id);
        //调用UserService实现修改User
        userService.update(user);
        return new Result(true,StatusCode.OK,"修改成功");
    }

    /***
     * 新增User数据
     * @param user
     * @return
     */
    @PostMapping
    public Result add(@RequestBody   User user){
        //调用UserService实现添加User
        userService.add(user);
        return new Result(true,StatusCode.OK,"添加成功");
    }

    /***
     * 该方法只允许 admin角色操作
     * 根据ID查询User数据
     * @param id
     * @return
     */
    @PreAuthorize("hasAnyAuthority('admin')")//只允许admin角色和user角色访问该方法
    @GetMapping({"/{id}","/load/{id}"})
    public Result<User> findById(@PathVariable String id){
        //调用UserService实现根据主键查询User
        User user = userService.findById(id);
        return new Result<User>(true,StatusCode.OK,"查询成功",user);
    }

    /***
     * 查询User全部数据
     * @return
     */
    @GetMapping
    public Result<List<User>> findAll(){
        //调用UserService实现查询所有User
        List<User> list = userService.findAll();
        return new Result<List<User>>(true, StatusCode.OK,"查询成功",list) ;
    }
}
