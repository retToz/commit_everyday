package demo.springcloud.servicea.controller;

import demo.springcloud.servicea.dao.TestMapper;
import demo.springcloud.servicea.po.Test;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ServiceA")
public class TestController {

    private final TestMapper testMapper;

    @GetMapping("/test")
    public void test() {
        System.out.println("test");
        Test test = new Test();
        test.setName("demo");
        testMapper.insert(test);
        //throw new RuntimeException("test exception");
    }
}
