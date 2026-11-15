package demo.springcloud.gateway.predicate;

import lombok.Data;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.function.Predicate;

// 自定义路由断言工厂
// 命名格式为： xxxRoutePredicateFactory  xxx为自定义名称
public class CustomRoutePredicateFactory extends AbstractRoutePredicateFactory<CustomRoutePredicateFactory.Config> {


    public CustomRoutePredicateFactory() {
        super(Config.class);

    }

    @Data
    static class Config{
        // 无参时为空即可
        // 有参数时需配置 shortcutFieldOrder() 方法
        String arg1;
        String arg2;
    }

    // 属性映射
    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("arg1", "arg2");
    }
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        // 根据需求对请求进行判断，返回true表示满足条件，返回false表示不满足条件
        return exchange -> true;
    }
}
