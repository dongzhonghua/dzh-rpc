package xyz.dsvshx.common.service;

import xyz.dsvshx.common.annotation.RpcInterface;

/**
 * @author dongzhonghua
 * Created on 2021-03-03
 */
@RpcInterface
public interface HelloService {
    String sayHello(String msg);
}
