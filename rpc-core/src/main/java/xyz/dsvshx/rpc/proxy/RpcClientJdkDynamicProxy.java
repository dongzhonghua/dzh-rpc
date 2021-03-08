package xyz.dsvshx.rpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.common.entity.RpcServiceProperties;
import xyz.dsvshx.common.protocol.RpcRequest;
import xyz.dsvshx.common.protocol.RpcResponse;
import xyz.dsvshx.rpc.netty.RpcClient;

/**
 * @author dongzhonghua
 * Created on 2021-03-03
 */
@Slf4j
public class RpcClientJdkDynamicProxy implements InvocationHandler {
    private Class<?> clazz;
    private final RpcServiceProperties rpcServiceProperties;
    private RpcClient rpcClient;


    public RpcClientJdkDynamicProxy(Class<?> clazz, RpcServiceProperties rpcServiceProperties,
            RpcClient rpcClient) {
        this.clazz = clazz;
        this.rpcServiceProperties = rpcServiceProperties;
        this.rpcClient = rpcClient;
    }

    // TODO: 2021/3/6 这里应该可以吧group和version传过来
    public <T> T create() {
        // TODO: 2021/3/3 这里有没有更好地实现方法，怎么能让用户直接调用而不感知呢
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[] {clazz},
                this
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
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
        request.setGroup(rpcServiceProperties.getGroup());
        request.setVersion(rpcServiceProperties.getVersion());
        log.info("请求内容: {}", request);
        RpcResponse rpcResponse = (RpcResponse) rpcClient.sendRpcRequest(request);
        log.info("得到返回结果：{}", rpcResponse.getResult());
        return rpcResponse.getResult();
    }


}
