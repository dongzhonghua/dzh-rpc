package xyz.dsvshx.rpc.provider;

import xyz.dsvshx.common.entity.RpcServiceProperties;

/**
 * @author dongzhonghua
 * Created on 2021-03-06
 */
public interface ServiceProvider {
    /**
     * @param service service object
     * @param serviceClass the interface class implemented by the service instance object
     * @param rpcServiceProperties service related attributes
     */
    void addService(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties);

    /**
     * @param rpcServiceProperties service related attributes
     * @return service object
     */
    Object getService(RpcServiceProperties rpcServiceProperties);

    /**
     * @param service service object
     * @param rpcServiceProperties service related attributes
     */
    void publishService(Object service, RpcServiceProperties rpcServiceProperties);

    /**
     * @param service service object
     */
    void publishService(Object service);
}
