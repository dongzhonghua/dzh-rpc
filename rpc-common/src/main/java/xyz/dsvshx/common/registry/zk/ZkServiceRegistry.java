package xyz.dsvshx.common.registry.zk;

import java.net.InetSocketAddress;

import org.apache.curator.framework.CuratorFramework;

import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.common.registry.ServiceRegistry;
import xyz.dsvshx.common.registry.zk.util.CuratorUtils;


@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {

    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createEphemeralNode(zkClient, servicePath);
    }
}
