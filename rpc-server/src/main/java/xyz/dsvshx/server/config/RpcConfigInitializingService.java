package xyz.dsvshx.server.config;

import java.util.Set;

import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.common.annotation.RpcInterface;

/**
 * @author dongzhonghua
 * Created on 2021-03-04
 */
@Configuration
@Slf4j
public class RpcConfigInitializingService implements ApplicationContextAware, InitializingBean {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Reflections reflections = new Reflections("xyz.dsvshx");
        DefaultListableBeanFactory beanFactory =
                (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        // 获取 @RpcInterfac 标注的接口
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(RpcInterface.class);
        for (Class<?> aClass : typesAnnotatedWith) {
            if (aClass.isInterface()) {
                // 创建代理对象，并注册到 spring 上下文。
                // beanFactory.registerSingleton(aClass.getSimpleName(), RpcClientJdkDynamicProxy.create(aClass));
                // log.info("注册bean:{}", aClass.getSimpleName());
            }
        }
    }

}
