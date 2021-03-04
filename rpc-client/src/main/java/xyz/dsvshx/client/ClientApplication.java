package xyz.dsvshx.client;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.common.service.HelloService;

/**
 * @author dongzhonghua
 * Created on 2021-03-03
 */
@SpringBootApplication
@Slf4j
public class ClientApplication {
    public static void main(String[] args) throws Exception {
        ApplicationContext ctx = SpringApplication.run(ClientApplication.class, args);

        // HelloService helloService = ProxyFactory.create(HelloService.class);
        // log.info("响应结果“: {}", helloService.sayHello("xxxxxxxxxxxxxxxx"));

        HelloService helloService = ctx.getBean(HelloService.class);
        System.out.println(helloService.sayHello("xxxxxxxxx"));
    }
}

