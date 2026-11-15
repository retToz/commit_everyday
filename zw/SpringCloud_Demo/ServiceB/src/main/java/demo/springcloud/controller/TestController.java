package demo.springcloud.controller;

import demo.springcloud.api.client.ServiceaClient;
import lombok.RequiredArgsConstructor;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ServiceB")
@RequiredArgsConstructor
public class TestController {

    private final ServiceaClient serviceaClient;

    @GetMapping("/test")
    @GlobalTransactional
    public String test(){
        // 通过 feign 调用其他服务，实现服务间通信
        serviceaClient.test();
        return "OK";
    }

}
