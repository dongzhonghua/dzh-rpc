package xyz.dsvshx.client.proxy;

import java.lang.reflect.Proxy;

/**
 * @author dongzhonghua
 * Created on 2021-03-03
 */
public class ProxyFactory {
    public static <T> T create(Class<T> interfaceClass) throws Exception {
        // TODO: 2021/3/3 这里有没有更好地实现方法，怎么能让用户直接调用而不感知呢
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] {interfaceClass},
                new RpcClientJdkDynamicProxy<T>(interfaceClass));
    }

}
