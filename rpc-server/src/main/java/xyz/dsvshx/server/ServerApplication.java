package xyz.dsvshx.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.rpc.netty.NettyServer;

/**
 * @author dongzhonghua
 * Created on 2021-03-03
 */
@SpringBootApplication(scanBasePackages = {"xyz.dsvshx"})
@Slf4j
public class ServerApplication {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext ctx = SpringApplication.run(ServerApplication.class, args);
        new NettyServer().start();
    }
}
