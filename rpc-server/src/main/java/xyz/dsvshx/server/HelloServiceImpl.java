package xyz.dsvshx.server;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.common.annotation.RpcService;
import xyz.dsvshx.common.service.HelloService;

/**
 * @author dongzhonghua
 * Created on 2021-03-03
 */
@Slf4j
@Service
@RpcService(group = "test1", version = "version1")
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String msg) {
        log.info("调用sayHello:{}", msg);
        return "调用sayHello:" + msg;
    }
}
