package com.changgou.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.oauth.controller
 * 登录跳转控制器
 ****/
@Controller
@RequestMapping(value = "/oauth")
public class LoginRedirect {

    /****
     * 登录页面跳转
     * SpringSecurity 自定义登录页
     *      /oauth/login
     *      /user/login
     * @return
     */
    @GetMapping(value = "/login")
    public String login(@RequestParam(value = "FROM",required = false,defaultValue = "http://www.changgou.com")String from, Model model){
        model.addAttribute("from",from);
        return "login";
    }
}
