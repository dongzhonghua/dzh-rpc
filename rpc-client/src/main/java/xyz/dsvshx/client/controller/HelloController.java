package xyz.dsvshx.client.controller;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.common.annotation.RpcReference;
import xyz.dsvshx.common.service.HelloService;

/**
 * @author dongzhonghua
 * Created on 2021-03-07
 */
@Slf4j
@Component
public class HelloController {

    @RpcReference(version = "version1", group = "test1")
    private HelloService helloService;

    public void test() throws InterruptedException {
        // String hello = helloService.sayHello("hello dzh");
        //如需使用 assert 断言，需要在 VM options 添加参数：-ea
        // Assert.assertEquals("调用sayHello:hello dzh", hello);
        // Thread.sleep(1000);
        for (int i = 0; i < 10; i++) {
            log.info(helloService.sayHello("hello " + i));
        }
    }
}
