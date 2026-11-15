package demo.springcloud.api.fallback;

import demo.springcloud.api.client.ServiceaClient;
import org.springframework.cloud.openfeign.FallbackFactory;

// 服务不可用时的降级反馈
public class CustomFallBack implements FallbackFactory<ServiceaClient> {
    @Override
    public ServiceaClient create(Throwable cause) {
        return new ServiceaClient() {
            @Override
            public void test() {
                System.out.println("服务不可用，已触发降级反馈");
            }
        };
    }
}
