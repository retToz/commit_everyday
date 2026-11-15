package demo.springcloud.api.decoder;

import demo.springcloud.api.domain.Result;
import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;

// 2.1、微服务业务异常
// 统一返回结果实体后，异常也会作为正常的返回结果，
// 可以通过 decoder 对 内部 code 进行解析
@AllArgsConstructor
public class CustomDecoder implements Decoder {

    // openfeign 默认的 decoder
    private Decoder delegate;

    @Override
    public Object decode(Response response, Type type) throws IOException{
        // 先让默认Decoder转换
        Object result = delegate.decode(response, type);

        // 判断统一返回
        if (result instanceof Result<?> res) {

            Integer code = res.getCode();

            if (code == null) {
                return result;
            }

            // 检查业务异常
            if (code < 200 || code >= 300) {

                throw new RuntimeException(
                        res.getMsg()
                );
            }
        }
        return result;
    }
}
