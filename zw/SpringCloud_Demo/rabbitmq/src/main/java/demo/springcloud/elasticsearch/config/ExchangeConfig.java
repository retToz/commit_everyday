package demo.springcloud.elasticsearch.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExchangeConfig {

    /*
      手动创建交换机和队列
      1、amqp 内置 ExchangeBuilder 和 queueBuilder 封装了创建方法
      2、直接 new() 创建
      3、通过注解的方式声明
      交换机和消息队列的绑定需另外配置
     */

    @Bean
    public FanoutExchange fanoutExchange() {
        // return ExchangeBuilder.fanoutExchange("test.exchange").build();
        return new FanoutExchange("test.fanout.exchange");
    }

    @Bean
    public DirectExchange directExchange(){
        // return ExchangeBuilder.directExchange("test.exchange").build();
        return new DirectExchange("test.direct.exchange");
    }

    @Bean
    public Queue queue1(){
        // return QueueBuilder.durable("test.queue1").build();
        return new Queue("test.queue1");
    }

    @Bean
    public Queue queue2(){
        // return QueueBuilder.durable("test.queue2").build();
        return new Queue("test.queue2");
    }

    @Bean
    public Binding binding1(){
        // return BindingBuilder.bind(queue1()).to(fanoutExchange());
        return new Binding("test.queue1", Binding.DestinationType.QUEUE, "test.fanout.exchange", "", null);
    }

    @Bean
    public Binding binding2(){
        // return BindingBuilder.bind(queue1()).to(directExchange()).with("test.routingkey");
        return new Binding("test.queue2", Binding.DestinationType.QUEUE, "test.direct.exchange", "test.routekey", null);
    }

    @Bean
    public DirectExchange errorMessageExchange(){
        return new DirectExchange("error.direct");
    }
    @Bean
    public Queue errorQueue(){
        return new Queue("error.queue", true);
    }
    @Bean
    public Binding errorBinding(Queue errorQueue, DirectExchange errorMessageExchange){
        return BindingBuilder.bind(errorQueue).to(errorMessageExchange).with("error");
    }
}
