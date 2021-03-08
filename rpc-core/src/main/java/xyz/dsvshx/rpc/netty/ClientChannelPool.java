package xyz.dsvshx.rpc.netty;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存客户端netty连接
 *
 * @author dongzhonghua
 * Created on 2021-03-07
 */
@Slf4j
public class ClientChannelPool {

    private final Map<String, Channel> channelMap;

    public ClientChannelPool() {
        channelMap = new ConcurrentHashMap<>();
    }

    public Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.getHostName() + inetSocketAddress.getPort();
        // determine if there is a connection for the corresponding address
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            // if so, determine if the connection is available, and if so, get it directly
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(key);
            }
        }
        return null;
    }

    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.getHostName() + inetSocketAddress.getPort();
        channelMap.put(key, channel);
        log.info("当前channel pool中元素：{}", channelMap);
    }

    public void remove(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        channelMap.remove(key);
        log.info("Channel map size :[{}]", channelMap.size());
    }

    public void shutdown() {
        for (Entry<String, Channel> channelEntry : channelMap.entrySet()) {
            log.info("关闭channel：{}", channelEntry.getValue());
            channelEntry.getValue().closeFuture().syncUninterruptibly();
        }
    }
}
