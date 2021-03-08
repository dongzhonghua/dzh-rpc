package xyz.dsvshx.rpc.netty;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.common.factory.SingletonFactory;
import xyz.dsvshx.common.protocol.RpcDecoder;
import xyz.dsvshx.common.protocol.RpcEncoder;
import xyz.dsvshx.common.protocol.RpcRequest;
import xyz.dsvshx.common.protocol.RpcResponse;
import xyz.dsvshx.common.protocol.serialize.JSONSerializer;
import xyz.dsvshx.common.registry.ServiceDiscovery;
import xyz.dsvshx.common.registry.zk.ZkServiceDiscovery;

/**
 * @author dongzhonghua
 * Created on 2021-03-03
 */
@Slf4j
@Getter
public class NettyRpcClient implements RpcClient {
    ClientChannelPool clientChannelPool;
    ServiceDiscovery serviceDiscovery;

    private EventLoopGroup eventLoopGroup;
    Bootstrap bootstrap;

    private ClientHandler clientHandler;
    private static final int MAX_RETRY = 5;
    private static final int TIMEOUT = 5000;
    private boolean isConnected = false;

    public NettyRpcClient() {
        eventLoopGroup = new NioEventLoopGroup();
        //启动类
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                //指定传输使用的Channel
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //添加编码器
                        pipeline.addLast(new RpcEncoder(RpcRequest.class, new JSONSerializer()));
                        //添加解码器
                        pipeline.addLast(new RpcDecoder(RpcResponse.class, new JSONSerializer()));
                        //请求处理类
                        pipeline.addLast(clientHandler);
                    }
                });
        this.clientChannelPool = SingletonFactory.getInstance(ClientChannelPool.class);
        this.serviceDiscovery = SingletonFactory.getInstance(ZkServiceDiscovery.class);
        this.clientHandler = SingletonFactory.getInstance(ClientHandler.class);
    }

    public Channel connect(String host, int port) {
        return connect(bootstrap, host, port, MAX_RETRY);
    }

    /**
     * 失败重连机制，参考Netty入门实战掘金小册
     */
    private Channel connect(Bootstrap bootstrap, String host, int port, int retry) {
        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.connect(host, port).addListener(future -> {
                if (future.isSuccess()) {
                    log.info("连接服务端成功");
                    isConnected = true;
                } else if (retry == 0) {
                    log.error("重试次数已用完，放弃连接");
                    isConnected = false;
                } else {
                    //第几次重连：
                    int order = (MAX_RETRY - retry) + 1;
                    //本次重连的间隔
                    int delay = 1 << order;
                    log.error("{} : 连接失败，第 {} 重连....", new Date(), order);
                    bootstrap.config().group()
                            .schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit.SECONDS);
                }
            }).sync();
        } catch (InterruptedException e) {
            log.error("连接失败：", e);
            e.printStackTrace();
        }
        if (channelFuture == null) {
            return null;
        }
        return channelFuture.channel();

    }

    // public Object sendRpcRequest(RpcRequest request) {
    //     // 获取服务地址
    //     String rpcServiceName = request.toRpcProperties().toRpcServiceName();
    //     InetSocketAddress socketAddress =
    //             serviceDiscovery.lookupService(rpcServiceName);
    //     log.info("发现服务{}地址：{}", rpcServiceName, socketAddress);
    //     //开启Netty 客户端，直连
    //     NettyRpcClient nettyRpcClient = new NettyRpcClient(socketAddress.getHostName(), socketAddress.getPort());
    //     log.info("开始连接服务端：{}", new Date());
    //     nettyRpcClient.connect();
    //     // TODO: 2021/3/3 有时候还没连接完就开始发送，导致失败,但是这种解决方法太粗糙
    //     // TODO: 2021/3/7 如何解决每次发送请求都需要连接客户端，这种耗时还挺多的
    //     // TODO: 2021/3/7 这个问题其实可以变成如何复用channel的问题
    //     // while (!nettyClient.isConnected()) {
    //     //     log.info("获取连接状态:{}", nettyClient.isConnected());
    //     //     Thread.sleep(10);
    //     // }
    //     RpcResponse send = nettyRpcClient.send(request);
    //     log.info("请求调用返回结果：{}", send.getResult());
    //     return send.getResult();
    // }

    /**
     * 发送消息
     */
    // public RpcResponse send(final RpcRequest request) {
    //     try {
    //         channel.writeAndFlush(request).await();
    //     } catch (InterruptedException e) {
    //         e.printStackTrace();
    //     }
    //     return clientHandler.getRpcResponse(request.getRequestId());
    // }
    @PreDestroy
    public void close() {
        eventLoopGroup.shutdownGracefully();
        clientChannelPool.shutdown();
        // channel.closeFuture().syncUninterruptibly();
    }


    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // build return value
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        // build rpc service name by rpcRequest
        String rpcServiceName = rpcRequest.toRpcProperties().toRpcServiceName();
        // get server address
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcServiceName);
        // get  server address related channel
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {

            // put unprocessed request
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcRequest);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }
        return clientHandler.getRpcResponse(rpcRequest.getRequestId());
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = clientChannelPool.get(inetSocketAddress);
        if (channel == null) {
            channel = connect(inetSocketAddress.getHostName(), inetSocketAddress.getPort());
            clientChannelPool.set(inetSocketAddress, channel);
        }
        return channel;
    }
}


