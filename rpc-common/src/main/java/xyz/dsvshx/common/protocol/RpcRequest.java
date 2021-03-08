package xyz.dsvshx.common.protocol;

import lombok.Data;
import lombok.ToString;
import xyz.dsvshx.common.entity.RpcServiceProperties;

/**
 * @author dongzhonghua
 * Created on 2021-03-06
 */

@Data
@ToString
public class RpcRequest {
    /**
     * 请求对象的ID
     */
    private String requestId;
    /**
     * 类名
     */
    private String className;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 参数类型
     */
    private Class<?>[] parameterTypes;
    /**
     * 入参
     */
    private Object[] parameters;

    /**
     * 服务的version
     */
    private String version;

    /**
     * 服务组
     */
    private String group;

    public RpcServiceProperties toRpcProperties() {
        return RpcServiceProperties.builder().serviceName(this.getClassName())
                .version(this.getVersion())
                .group(this.getGroup()).build();
    }
}
