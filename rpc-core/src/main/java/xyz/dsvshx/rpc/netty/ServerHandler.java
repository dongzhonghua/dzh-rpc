package xyz.dsvshx.rpc.netty;

import java.lang.reflect.Method;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.common.factory.SingletonFactory;
import xyz.dsvshx.common.protocol.RpcRequest;
import xyz.dsvshx.common.protocol.RpcResponse;
import xyz.dsvshx.rpc.provider.ServiceProvider;
import xyz.dsvshx.rpc.provider.ServiceProviderImpl;

/**
 * @author dongzhonghua
 * Created on 2021-03-03
 */
@Component
@Slf4j
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<RpcRequest> implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        try {
            Object handler = handler(rpcRequest);
            log.info("获取返回结果: {} ", handler);
            rpcResponse.setResult(handler);
        } catch (Throwable throwable) {
            rpcResponse.setError(throwable.toString());
            throwable.printStackTrace();
        }
        ctx.writeAndFlush(rpcResponse);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    private Object handler(RpcRequest request) throws Exception {
        // Class<?> clazz = Class.forName(request.getClassName());
        // Object serviceBean = applicationContext.getBean(clazz);
        // log.info("调用的serviceBean:{}", serviceBean);
        // Class<?> serviceClass = serviceBean.getClass();
        // log.info("调用的serviceClass name:{}", serviceClass);
        // String methodName = request.getMethodName();
        // log.info("调用的方法 name:{}", methodName);
        // Class<?>[] parameterTypes = request.getParameterTypes();
        // Object[] parameters = request.getParameters();
        // 使用cglib
        log.info("开始调用CGLIB动态代理执行服务端方法...");
        // 方法1. 用代理调用，但是多此一举。
        // 为啥要用代理？直接反射调用也一样啊。
        // return new CglibProxy().invoke(serviceClass, methodName, parameterTypes, parameters);
        // 方法2. 直接反射调用
        // Method method = serviceClass.getMethod(methodName, parameterTypes);
        // method.setAccessible(true);
        // return method.invoke(serviceClass.newInstance(), parameters);
        // 方法3. 通过注册的服务调用
        return handleRequest(request);
    }


    /**
     * Processing rpcRequest: call the corresponding method, and then return the method
     */
    public Object handleRequest(RpcRequest rpcRequest) {
        Object service = serviceProvider.getService(rpcRequest.toRpcProperties());
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * get method execution results
     *
     * @param rpcRequest client request
     * @param service service object
     * @return the result of the target method execution
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result = null;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getClassName(),
                    rpcRequest.getMethodName());
        } catch (Exception e) {
            log.error(String.valueOf(e));
        }
        return result;
    }
}
