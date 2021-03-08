package xyz.dsvshx.rpc.netty;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import xyz.dsvshx.common.protocol.RpcResponse;

/**
 * @author dongzhonghua
 * Created on 2021-03-03
 */
@Sharable
@Slf4j
public class ClientHandler extends ChannelDuplexHandler {
    /**
     * 使用Map维护请求对象ID与响应结果Future的映射关系
     */
    // private final Map<String, DefaultFuture> futureMap = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<RpcResponse>> futureMap = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RpcResponse) {
            //获取响应对象
            RpcResponse response = (RpcResponse) msg;
            CompletableFuture<RpcResponse> responseCompletableFuture = futureMap.get(response.getRequestId());
            responseCompletableFuture.complete(response);
        }
        super.channelRead(ctx, msg);
    }


    // 一开始用这种方式吧future put进去，但是有可能还没put进去就已经在get了，导致空指针，但是用DefaultFuture的时候就不会有这个问题
    // @Override
    // public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    //     if (msg instanceof RpcRequest) {
    //         RpcRequest request = (RpcRequest) msg;
    //         //发送请求对象之前，先把请求ID保存下来，并构建一个与响应Future的映射关系
    //         futureMap.putIfAbsent(request.getRequestId(), new CompletableFuture<>());
    //
    //     }
    //     super.write(ctx, msg, promise);
    // }

    public RpcResponse getRpcResponse(String requestId) {
        try {
            CompletableFuture<RpcResponse> responseCompletableFuture = new CompletableFuture<>();
            futureMap.putIfAbsent(requestId, responseCompletableFuture);
            return responseCompletableFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //获取成功以后，从map中移除
            futureMap.remove(requestId);
        }
        return null;
    }

    //一开始写成read了，这两个方法有什么区别吗？
    // @Override
    // public void read(ChannelHandlerContext msg) throws Exception {
    //     if (msg instanceof RpcResponse) {
    //         //获取响应对象
    //         RpcResponse response = (RpcResponse) msg;
    //         log.info("client response:{}", response);
    //         DefaultFuture defaultFuture =
    //                 futureMap.get(response.getRequestId());
    //         //将结果写入DefaultFuture
    //         defaultFuture.setResponse(response);
    //     }
    //     super.read(msg);
    // }
}
