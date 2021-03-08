package xyz.dsvshx.rpc.provider;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.common.entity.RpcServiceProperties;
import xyz.dsvshx.common.registry.ServiceRegistry;
import xyz.dsvshx.common.registry.zk.ZkServiceRegistry;
import xyz.dsvshx.rpc.netty.NettyServer;


@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    /**
     * key: rpc service name(interface name + version + group)
     * value: service object
     */
    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;


    public ServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = new ZkServiceRegistry();
        // TODO: 2021/3/6 这里可以用spi机制加载
        // serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }

    @Override
    public void addService(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties) {
        String rpcServiceName = rpcServiceProperties.toRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, service);
        log.info("Add service: {} and interfaces:{}", rpcServiceName, service.getClass().getInterfaces());
    }

    /**
     * 反射调用是使用这个来完成
     *
     * @param rpcServiceProperties service related attributes
     */
    @Override
    public Object getService(RpcServiceProperties rpcServiceProperties) {
        Object service = serviceMap.get(rpcServiceProperties.toRpcServiceName());
        if (null == service) {
            // TODO: 2021/3/6 异常体系后面再来吧
            log.error("service not found");
            // throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(Object service) {
        this.publishService(service, RpcServiceProperties.builder().group("").version("").build());
    }

    /**
     * 两个功能，将本地服务注册到zk注册中心并缓存起来。
     * @param service service object
     * @param rpcServiceProperties service related attributes
     */
    @Override
    public void publishService(Object service, RpcServiceProperties rpcServiceProperties) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            Class<?> serviceRelatedInterface = service.getClass().getInterfaces()[0];
            String serviceName = serviceRelatedInterface.getCanonicalName();
            rpcServiceProperties.setServiceName(serviceName);
            this.addService(service, serviceRelatedInterface, rpcServiceProperties);
            serviceRegistry.registerService(rpcServiceProperties.toRpcServiceName(),
                    new InetSocketAddress(host, NettyServer.port));
            log.info(String.valueOf(registeredService));
            log.info(String.valueOf(serviceMap));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }

}
