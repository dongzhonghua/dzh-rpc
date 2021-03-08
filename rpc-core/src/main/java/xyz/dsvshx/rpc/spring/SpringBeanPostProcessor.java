package xyz.dsvshx.rpc.spring;

import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.common.annotation.RpcReference;
import xyz.dsvshx.common.annotation.RpcService;
import xyz.dsvshx.common.entity.RpcServiceProperties;
import xyz.dsvshx.common.factory.SingletonFactory;
import xyz.dsvshx.rpc.netty.NettyRpcClient;
import xyz.dsvshx.rpc.netty.RpcClient;
import xyz.dsvshx.rpc.provider.ServiceProvider;
import xyz.dsvshx.rpc.provider.ServiceProviderImpl;
import xyz.dsvshx.rpc.proxy.RpcClientJdkDynamicProxy;

/**
 * 注册服务是使用spring的机制
 *
 * @author dongzhonghua
 * Created on 2021-03-06
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcClient rpcClient;

    public SpringBeanPostProcessor() {
        this.rpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
        ;
        this.serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
        // TODO: 2021/3/7 rpcRequestTransport是干什么的
        // this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            // get RpcService annotation
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            // build RpcServiceProperties
            RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                    .group(rpcService.group()).version(rpcService.version()).build();
            log.info("RpcServiceProperties:{}", rpcServiceProperties.toRpcServiceName());
            serviceProvider.publishService(bean, rpcServiceProperties);
        }
        return bean;
    }

    // 怎么在这里
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                Class<?> service = declaredField.getType();
                RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                        .group(rpcReference.group()).version(rpcReference.version()).build();
                Object clientProxy = new RpcClientJdkDynamicProxy(service, rpcServiceProperties, rpcClient).create();
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, clientProxy);
                    log.info("rpc服务依赖注入：bean-{}, clientProxy-{}", bean.getClass(), clientProxy.getClass());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
        return bean;
    }
}

