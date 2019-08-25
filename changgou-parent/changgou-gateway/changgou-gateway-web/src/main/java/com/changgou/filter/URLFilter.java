package com.changgou.filter;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.filter
 *  拦截过滤所有请求路径，检查是否需要用户登录
 ****/
public class URLFilter {

    private static final String urls = "/api/user/add,/api/user/login";

    /****
     * 判断请求路径是否需要授权
     * 不需要授权  false
     * 需要授权  true
     * @param uri
     * @return
     */
    public static boolean hasAuthorize(String uri){
        String[] urlArrays = urls.split(",");
        for (String urlArray : urlArrays) {
            if(uri.equals(urlArray)){
                return false;
            }
        }
        return true;
    }

}
