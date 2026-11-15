package demo.springcloud.api.config;

import demo.springcloud.api.decoder.CustomDecoder;
import demo.springcloud.api.decoder.CustomErrorDecoder;
import demo.springcloud.api.fallback.CustomFallBack;
import demo.springcloud.api.interceptor.CustomRequestInterceptor;
import demo.springcloud.api.interceptor.CustomResponseInterceptor;
import feign.Logger;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class DefaultFeignConfig {

    // OpenFeign 只会在 FeignClient 所在包的日志级别为DEBUG时，
    // 才会输出日志。而且其日志级别有4级：：
    // NONE    不记录
    // BASIC   请求方法、URL、状态码、耗时
    // HEADERS 增加请求头响应头
    // FULL	   请求头、响应头、请求体、响应体
    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.NONE;
    }

    @Bean
    public Decoder decoder(
            ObjectFactory<HttpMessageConverters> converters) {
        return  new CustomDecoder(new SpringDecoder(converters));
    }

    @Bean
    public ErrorDecoder errorDecoder(){
        return new CustomErrorDecoder();
    }

    @Bean
    public CustomResponseInterceptor customResponseInterceptor(){
        return new CustomResponseInterceptor();
    }

    @Bean
    public CustomRequestInterceptor customRequestInterceptor(){
        return new CustomRequestInterceptor();
    }

    @Bean
    public CustomFallBack customFallBack(){
        return new CustomFallBack();
    }
}
