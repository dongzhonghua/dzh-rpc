package xyz.dsvshx.rpc.proxy;

import java.lang.reflect.Method;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dongzhonghua
 * Created on 2021-03-03
 */

@Slf4j
public class CglibProxy implements MethodInterceptor {


    public Object invoke(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object[] parameters) {
        //使用cglib生成代理
        //1.创建核心类
        Enhancer enhancer = new Enhancer();
        //2.为哪个类生成代理
        enhancer.setSuperclass(clazz);
        //3.设置回调，相当于JDK动态代理中的invoke方法
        enhancer.setCallback(this);
        //4.创建代理对象并执行方法
        Method method;
        Object result = null;
        try {
            Object proxy = enhancer.create();
            method = proxy.getClass().getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            result = method.invoke(proxy, parameters);
        } catch (Exception e) {
            log.info("方法调用失败");
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args,
            MethodProxy methodProxy) throws Throwable {
        log.info("调用代理对象方法：{}#{}", proxy, method);
        //调用代理对象的方法，相当也调用父类的方法
        return methodProxy.invokeSuper(proxy, args);
    }

    public static void main(String[] args) {
        Class<String> stringClass = String.class;
        Class<?>[] classes = new Class[1];
        classes[0] = stringClass;
        Object[] o = new Object[1];
        o[0] = "xxxx";
    }
}
