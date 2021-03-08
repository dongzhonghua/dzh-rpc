package xyz.dsvshx.rpc.netty;

import xyz.dsvshx.common.protocol.RpcRequest;

/**
 * @author dongzhonghua
 * Created on 2021-03-07
 */
public interface RpcClient {
    Object sendRpcRequest(RpcRequest request);
}
