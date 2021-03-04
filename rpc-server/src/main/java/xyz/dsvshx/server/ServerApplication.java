package xyz.dsvshx.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dongzhonghua
 * Created on 2021-03-03
 */
@SpringBootApplication
@Slf4j
public class ServerApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(ServerApplication.class, args);
    }
}
