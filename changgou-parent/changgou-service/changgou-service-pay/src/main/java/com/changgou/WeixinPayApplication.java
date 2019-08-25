package com.changgou;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou
 ****/
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})//禁止加载数据源对象
@EnableEurekaClient
public class WeixinPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeixinPayApplication.class,args);
    }

    /***
     * 用于读取properties/yml配置文件信息数据
     */
    @Autowired
    private Environment env;

    /**********************************************普通订单队列信息***********************************************************/
    /****
     * 创建交换机
     */
    @Bean
    public Exchange payExchange(){
        return new DirectExchange(env.getProperty("mq.pay.exchange.order"),true,false);
    }


    /****
     * 创建队列
     */
    @Bean
    public Queue payQueue(){
        return new Queue(env.getProperty("mq.pay.queue.order"));
    }


    /***
     * 队列绑定指定交换机
     */
    @Bean
    public Binding payBinding(Queue payQueue,Exchange payExchange){
        return BindingBuilder.bind(payQueue).to(payExchange).with(env.getProperty("mq.pay.routing.key")).noargs();
    }


    /****************************************秒杀订单队列*************************************************/
    /****
     * 创建交换机
     */
    @Bean
    public Exchange seckillExchange(){
        return new DirectExchange(env.getProperty("mq.pay.exchange.seckillorder"),true,false);
    }


    /****
     * 创建队列
     */
    @Bean
    public Queue seckillQueue(){
        return new Queue(env.getProperty("mq.pay.queue.seckillorder"));
    }


    /***
     * 队列绑定指定交换机
     */
    @Bean
    public Binding seckillBinding(Queue seckillQueue,Exchange seckillExchange){
        return BindingBuilder.bind(seckillQueue).to(seckillExchange).with(env.getProperty("mq.pay.routing.seckillkey")).noargs();
    }

}