package xyz.dsvshx.client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.client.netty.NettyClient;
import xyz.dsvshx.common.protocol.RpcRequest;
import xyz.dsvshx.common.protocol.RpcResponse;

/**
 * @author dongzhonghua
 * Created on 2021-03-03
 */
@Slf4j
public class RpcClientJdkDynamicProxy<T> implements InvocationHandler {
    private Class<T> clazz;

    public RpcClientJdkDynamicProxy(Class<T> clazz) throws Exception {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest();
        String requestId = UUID.randomUUID().toString();

        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();

        Class<?>[] parameterTypes = method.getParameterTypes();

        request.setRequestId(requestId);
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParameterTypes(parameterTypes);
        request.setParameters(args);
        log.info("请求内容: {}", request);
        //开启Netty 客户端，直连
        NettyClient nettyClient = new NettyClient("127.0.0.1", 8888);
        log.info("开始连接服务端：{}", new Date());
        nettyClient.connect();
        // TODO: 2021/3/3 有时候还没连接完就开始发送，导致失败，这种情况有没有解决方案？
        // while (!nettyClient.getIsConnected()) {
        //     log.info("获取连接状态:{}", nettyClient.getIsConnected());
        //     Thread.sleep(100);
        // }
        Thread.sleep(3000);
        RpcResponse send = nettyClient.send(request);
        log.info("请求调用返回结果：{}", send.getResult());
        return send.getResult();
    }
}
