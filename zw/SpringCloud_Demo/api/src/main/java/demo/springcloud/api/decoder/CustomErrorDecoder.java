package demo.springcloud.api.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;

// 1、服务请求的默认异常
// Feign 对非 2xx 状态码会进入 ErrorDecoder
// 可以通过 ErrorDecoder 自定义错误处理的返回
public class CustomErrorDecoder implements ErrorDecoder {

    // 抛出异常给异常处理
    @Override
    public Exception decode(String s, Response response) {
        if(response.status()==404){
            return new RuntimeException(
                    "用户不存在"
            );
        }

        if(response.status()==500){
            return new RuntimeException(
                    "远程服务异常"
            );
        }

        return new Default()
                .decode(s,response);
    }
}
