package xyz.dsvshx.server.netty;

import java.lang.reflect.InvocationTargetException;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.common.protocol.RpcRequest;
import xyz.dsvshx.common.protocol.RpcResponse;
import xyz.dsvshx.server.proxy.CglibProxy;

/**
 * @author dongzhonghua
 * Created on 2021-03-03
 */
@Component
@Slf4j
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<RpcRequest> implements ApplicationContextAware {
    private ApplicationContext applicationContext;

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

    /**
     * 服务端使用代理处理请求
     */
    // private Object handler(RpcRequest request) throws ClassNotFoundException, InvocationTargetException {
    //     //使用Class.forName进行加载Class文件
    //     Class<?> clazz = Class.forName(request.getClassName());
    //     Object serviceBean = applicationContext.getBean(clazz);
    //     log.info("serviceBean: {}", serviceBean);
    //     Class<?> serviceClass = serviceBean.getClass();
    //     log.info("serverClass:{}", serviceClass);
    //     String methodName = request.getMethodName();
    //
    //     Class<?>[] parameterTypes = request.getParameterTypes();
    //     Object[] parameters = request.getParameters();
    //
    //     //使用CGLIB Reflect
    //     FastClass fastClass = FastClass.create(serviceClass);
    //     FastMethod fastMethod = fastClass.getMethod(methodName, parameterTypes);
    //     log.info("开始调用CGLIB动态代理执行服务端方法...");
    //     return fastMethod.invoke(serviceBean, parameters);
    // }

    private Object handler(RpcRequest request) throws ClassNotFoundException, InvocationTargetException {
        Class<?> clazz = Class.forName(request.getClassName());
        Object serviceBean = applicationContext.getBean(clazz);
        log.info("调用的serviceBean:{}", serviceBean);
        Class<?> serviceClass = serviceBean.getClass();
        log.info("调用的serviceClass name:{}", serviceClass);
        String methodName = request.getMethodName();
        log.info("调用的方法 name:{}", methodName);
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        // 使用cglib
        log.info("开始调用CGLIB动态代理执行服务端方法...");
        return new CglibProxy().invoke(serviceClass, methodName, parameterTypes, parameters);
    }
}
