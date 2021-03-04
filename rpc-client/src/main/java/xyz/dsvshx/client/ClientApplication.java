package xyz.dsvshx.client;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.client.proxy.ProxyFactory;
import xyz.dsvshx.common.service.HelloService;

/**
 * @author dongzhonghua
 * Created on 2021-03-03
 */
@SpringBootApplication
@Slf4j
public class ClientApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(ClientApplication.class, args);
        HelloService helloService = ProxyFactory.create(HelloService.class);
        log.info("响应结果“: {}", helloService.sayHello("xxxxxxxxxxxxxxxx"));
    }
}

