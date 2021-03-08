package xyz.dsvshx.common.loadbalance;

import java.util.List;

/**
 * @author dongzhonghua
 * Created on 2021-03-07
 */
public interface LoadBalance {
    /**
     * Choose one from the list of existing service addresses list
     *
     * @param serviceAddresses Service address list
     * @return target service address
     */
    String selectServiceAddress(List<String> serviceAddresses, String rpcServiceName);
}
