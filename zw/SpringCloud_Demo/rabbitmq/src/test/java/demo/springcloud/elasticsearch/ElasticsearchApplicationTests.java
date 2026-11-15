package demo.springcloud.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

import static java.lang.Thread.sleep;

@Slf4j
@SpringBootTest
class ElasticsearchApplicationTests {

    @Autowired
    private RabbitTemplate rabbitTemplate;


    // rabbitTemolate 发送消息
    // 交换机： test.exchange
    // 路由键： test.routing.key
    // 消息： test message
    @Test
    void contextLoads() {
        rabbitTemplate.convertAndSend("test.exchange","test.routing.key","test message");
        HashMap<String,String> map = new HashMap<>();
        map.put("name","test");
        map.put("content","test");
        rabbitTemplate.convertAndSend("test.fanout.exchange",null,map);
        try {
            sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // confirm 回调
    // 须在请求中手动添加 CorrelationData
    @Test
    void confirmTest(){
        // 1.创建CorrelationData
        CorrelationData cd = new CorrelationData();
        // 2.给Future添加ConfirmCallback
        cd.getFuture().whenComplete((result, ex) -> {
            if (ex != null) {
                // 2.1.Future发生异常时的处理逻辑，基本不会触发
                log.error("send message fail", ex);
                return;
            }
            // 2.2.Future接收到回执的处理逻辑，参数中的result就是回执内容
            if(result.isAck()){ // result.isAck()，boolean类型，true代表ack回执，false 代表 nack回执
                log.debug("发送消息成功，收到 ack!");
            }else{ // result.getReason()，String类型，返回nack时的异常描述
                log.error("发送消息失败，收到 nack, reason : {}", result.getReason());
            }
        });
        // 3.发送消息
        rabbitTemplate.convertAndSend("hmall.direct", "q", "hello", cd);
    }

}
