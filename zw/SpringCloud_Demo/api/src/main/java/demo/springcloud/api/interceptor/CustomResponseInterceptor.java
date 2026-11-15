package demo.springcloud.api.interceptor;

import demo.springcloud.api.domain.Result;
import feign.InvocationContext;
import feign.ResponseInterceptor;

// 2.2、处理非 HTTP 请求的异常
public class CustomResponseInterceptor implements ResponseInterceptor {

    @Override
    public Object intercept(InvocationContext invocationContext, Chain chain) throws Exception {
        // 先执行后续流程
        Object result =
                chain.next(invocationContext);
        // 这里拿到的是 Decoder 后的结果
        if (result instanceof Result<?> response) {

            Integer code =
                    response.getCode();

            // 微服务内部异常
            if (code < 200 || code >= 300) {

                throw new RuntimeException(
                        response.getMsg()
                );
            }
        }
        return result;
    }
}
