package xyz.dsvshx.server.netty;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
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
@Component
@Slf4j
public class NettyServer implements InitializingBean {
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    @Autowired
    private ServerHandler serverHandler;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 这种方式启动会不会有问题啊，有可能还没有启动起来但是服务已经来了
        start();
    }

    private void start() {
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4));
                        pipeline.addLast(new RpcEncoder(RpcResponse.class, new JSONSerializer()));
                        pipeline.addLast(new RpcDecoder(RpcRequest.class, new JSONSerializer()));
                        pipeline.addLast(serverHandler);

                    }
                });
        bind(serverBootstrap, 8888);
    }

    /**
     * 如果端口绑定失败，端口数+1,重新绑定
     */
    private void bind(final ServerBootstrap serverBootstrap, int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                log.info("端口[ {} ] 绑定成功", port);
            } else {
                log.error("端口[ {} ] 绑定失败", port);
                bind(serverBootstrap, port + 1);
            }
        });
    }

    @PreDestroy
    public void destory() throws InterruptedException {
        boss.shutdownGracefully().sync();
        worker.shutdownGracefully().sync();
        log.info("关闭Netty");
    }
}
