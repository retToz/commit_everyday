package demo.springcloud.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// 自定义局部过滤器
public class CustomGateWayFilterFactory extends AbstractGatewayFilterFactory<Object> {
    @Override
    public GatewayFilter apply(Object config) {
        return new GatewayFilter(){
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                // TODO: add your custom filter logic
                System.out.println("自定义局部过滤器1");
                // 继续执行下一个过滤器
                return chain.filter(exchange)
                        .then(Mono.fromRunnable(() -> {
                            // 请求完成后的逻辑，顺序与filter方法相反，这里可以添加日志记录、权限校验等逻辑
                            System.out.println("请求完成");
                        }));
            }
        };
    }
}
