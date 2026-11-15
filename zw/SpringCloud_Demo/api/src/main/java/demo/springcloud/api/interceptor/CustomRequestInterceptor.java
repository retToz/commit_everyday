package demo.springcloud.api.interceptor;

import feign.RequestInterceptor;

// 修改请求体的属性
public class CustomRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(feign.RequestTemplate template) {
        // Add custom headers or authentication tokens here
        template.header("token", "123456");
    }
}
