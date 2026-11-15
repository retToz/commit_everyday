package demo.springcloud.elasticsearch.listener;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// 通过注解声明交换机和消息队列
@Component
public class CustomListener2 {

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "test.queue", durable = "true"),
            exchange = @Exchange(name = "test.exchange", durable = "true", type = "direct"),
            key = "test.routing.key"
    ))
    public void listener1(String message){
        System.out.println("Received message: " + message);
    }
}
