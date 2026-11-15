package demo.springcloud.api.client;

import demo.springcloud.api.config.DefaultFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// 在 @EnableFeignClient 处添加则为全局配置 configuration = DefaultFeignConfig.class
@FeignClient(value = "Service-a", path="/ServiceA")
public interface ServiceaClient{
    @GetMapping("/test")
    void test();
}
