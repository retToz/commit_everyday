package demo.springcloud.gateway.filter;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

// 自定义局部过滤器（带参）
public class Custom2GatewayFilterFactory extends AbstractGatewayFilterFactory<Custom2GatewayFilterFactory.Config> {

    public Custom2GatewayFilterFactory() {
        super(Config.class);
    }

    @Data
    static class Config {
        // 配置项
        String arg1;
        String arg2;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("arg1", "arg2");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                // TODO: add your custom filter logic
                System.out.println("Custom2GatewayFilterFactory apply: " + config);
                return chain.filter(exchange);
            }
        };
    }

}
