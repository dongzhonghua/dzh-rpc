package xyz.dsvshx.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author dongzhonghua
 * Created on 2021-03-06
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceProperties {
    /**
     * service version
     */
    private String version;
    /**
     * when the interface has multiple implementation classes, distinguish by group
     */
    private String group;
    private String serviceName;

    public String toRpcServiceName() {
        return this.getServiceName() + "-" + this.getGroup() + "-" + this.getVersion();
    }
}
