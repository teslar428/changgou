package entity;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/*****
 * @Author: www.itheima.com
 * @Description: entity
 ****/
public class FeignRequestInerceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        /***
         * 当前线程请求的信息封装
         * 1:多线程隔离
         * 2:信号量隔离
         */
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            //取出request
            HttpServletRequest request = attributes.getRequest();
            //获取所有头文件信息的key
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    //头文件的key
                    String name = headerNames.nextElement();
                    System.out.println("头信息："+name);
                    //头文件的value
                    String values = request.getHeader(name);
                    //将令牌数据添加到头文件中
                    template.header(name, values);
                }
            }
        }
    }
}
