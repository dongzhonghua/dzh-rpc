package xyz.dsvshx.common.registry.zk;

import java.net.InetSocketAddress;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;

import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.common.loadbalance.LoadBalance;
import xyz.dsvshx.common.loadbalance.loadbalancer.ConsistentHashLoadBalance;
import xyz.dsvshx.common.registry.ServiceDiscovery;
import xyz.dsvshx.common.registry.zk.util.CuratorUtils;

/**
 * service discovery based on zookeeper
 *
 * @author shuang.kou
 * @createTime 2020年06月01日 15:16:00
 */
@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZkServiceDiscovery() {
        // 使用spi的方式加载
        // this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
        this.loadBalance = new ConsistentHashLoadBalance();
    }

    @Override
    public InetSocketAddress lookupService(String rpcServiceName) {
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (serviceUrlList == null || serviceUrlList.size() == 0) {
            // throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
            log.error("service not found");
        }
        // load balancing
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcServiceName);
        // String targetServiceUrl = serviceUrlList.stream().findFirst().orElse("");
        log.info("Successfully found the service address:[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
