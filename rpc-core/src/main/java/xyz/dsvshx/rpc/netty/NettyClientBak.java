package xyz.dsvshx.rpc.netty;


import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.common.protocol.RpcDecoder;
import xyz.dsvshx.common.protocol.RpcEncoder;
import xyz.dsvshx.common.protocol.RpcRequest;
import xyz.dsvshx.common.protocol.RpcResponse;
import xyz.dsvshx.common.protocol.serialize.JSONSerializer;

/**
 * @author dongzhonghua
 * Created on 2021-03-03
 */
@Slf4j
public class NettyClientBak {
    private EventLoopGroup eventLoopGroup;
    private Channel channel;
    private ClientHandler clientHandler;
    private String host;
    private Integer port;
    private static final int MAX_RETRY = 5;
    private static final int TIMEOUT = 5000;


    /**
     * 原始的netty客户端，但是这种方式只能每一次都要发起连接，不能重用channel，所以新的客户端在{{@link NettyRpcClient}}
     * @param host
     * @param port
     */
    public NettyClientBak(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        clientHandler = new ClientHandler();
        eventLoopGroup = new NioEventLoopGroup();
        //启动类
        Bootstrap bootstrap = new Bootstrap();
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
        connect(bootstrap, host, port, MAX_RETRY);
    }

    /**
     * 失败重连机制，参考Netty入门实战掘金小册
     */
    private void connect(Bootstrap bootstrap, String host, int port, int retry) {
        ChannelFuture channelFuture = bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()) {
                log.info("连接服务端成功");
            } else if (retry == 0) {
                log.error("重试次数已用完，放弃连接");
            } else {
                //第几次重连：
                int order = (MAX_RETRY - retry) + 1;
                //本次重连的间隔
                int delay = 1 << order;
                log.error("{} : 连接失败，第 {} 重连....", new Date(), order);
                bootstrap.config().group()
                        .schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit.SECONDS);
            }
        });
        channel = channelFuture.channel();
    }

    /**
     * 发送消息
     */
    public RpcResponse send(final RpcRequest request) {
        try {
            channel.writeAndFlush(request).await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return clientHandler.getRpcResponse(request.getRequestId());
    }

    @PreDestroy
    public void close() {
        eventLoopGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
    }
}



