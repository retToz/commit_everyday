package demo.springcloud.elasticsearch.config;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MQConfig {

    private final Map<String, MessageWrapper> retryCache = new ConcurrentHashMap<>();
    private final RabbitTemplate rabbitTemplate;

    // 配置 mq 的 returnback 和 confirm 回调
    @PostConstruct
    public void init() {
        rabbitTemplate.setReturnsCallback(returned ->{
            System.out.println("触发return callback,");
            System.out.println("exchange: " + returned.getExchange());
            System.out.println("routingKey: " + returned.getRoutingKey());
            System.out.println("message: " + returned.getMessage());
            System.out.println("replyCode: " + returned.getReplyCode());
            System.out.println("replyText: " + returned.getReplyText());
        });
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
                    if (correlationData == null) return;
                    String msgId = correlationData.getId();

                    if (ack) {
                        log.info("✅ 消息 [{}] 投递成功", msgId);
                        retryCache.remove(msgId);
                    } else {
                        log.warn("❌ 消息 [{}] 投递失败，原因：{}", msgId, cause);
                        retryCache.computeIfPresent(msgId, (id, wrapper) -> {
                            wrapper.setLastFailReason(cause);
                            wrapper.incrementRetry();
                            return wrapper;
                        });
                    }
        });

        // 消息发送失败重试机制
        startRetryTask();
    }

    // 在 mq 中，消息转化器默认为 jdk 自带的，其容量较大且不易查看，
    // 这里通过自定义消息转化器转化为 json 格式
    @Bean
    public MessageConverter messageConverter(){
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setCreateMessageIds(true);
        return converter;
    }

    @Bean
    public MessageRecoverer republishMessageRecoverer(RabbitTemplate rabbitTemplate){
        return new RepublishMessageRecoverer(rabbitTemplate, "error.direct", "error");
    }

    // 重试消息类
    @Data
    @AllArgsConstructor
    static class MessageWrapper {
        private String exchange;
        private String routingKey;
        private String message;
        private CorrelationData correlationData;
        private int retryCount = 0;
        private String lastFailReason;
        private long lastSendTime = System.currentTimeMillis();

        MessageWrapper(String exchange, String routingKey, String message, CorrelationData correlationData) {
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.message = message;
            this.correlationData = correlationData;
        }

        void incrementRetry() {
            this.retryCount++;
        }
    }

    public void send(String exchange, String routingKey, String msg) {
        String msgId = UUID.randomUUID().toString();
        CorrelationData data = new CorrelationData(msgId);

        MessageWrapper wrapper = new MessageWrapper(exchange, routingKey, msg, data);
        retryCache.put(msgId, wrapper);

        rabbitTemplate.convertAndSend(exchange, routingKey, msg, message -> {
            message.getMessageProperties().setMessageId(msgId);
            return message;
        }, data);

        log.info("重新向 mq 发送消息 [{}]：{}", msgId, msg);
    }

    private void startRetryTask() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            retryCache.values().forEach(wrapper -> {
                if (wrapper.getRetryCount() > 3) {
                    log.error("💥 消息 [{}] 超过最大重试次数，放弃。原因：{}", wrapper.getCorrelationData().getId(), wrapper.getLastFailReason());
                    retryCache.remove(wrapper.getCorrelationData().getId());
                    return;
                }

                long elapsed = System.currentTimeMillis() - wrapper.getLastSendTime();
                if (elapsed > 5000) { // 每5秒检查一次
                    log.warn("🔁 重发消息 [{}]（第 {} 次）", wrapper.getCorrelationData().getId(), wrapper.getRetryCount() + 1);
                    wrapper.incrementRetry();
                    wrapper.setLastSendTime(System.currentTimeMillis());
                    rabbitTemplate.convertAndSend(wrapper.getExchange(), wrapper.getRoutingKey(),
                            wrapper.getMessage(), wrapper.getCorrelationData());
                }
            });
        }, 5, 5, TimeUnit.SECONDS);
    }

}
