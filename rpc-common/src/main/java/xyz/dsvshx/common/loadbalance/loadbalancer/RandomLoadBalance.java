package xyz.dsvshx.common.loadbalance.loadbalancer;

import java.util.List;
import java.util.Random;

import xyz.dsvshx.common.loadbalance.AbstractLoadBalance;

/**
 * @author dongzhonghua
 * Created on 2021-03-07
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses, String rpcServiceName) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
