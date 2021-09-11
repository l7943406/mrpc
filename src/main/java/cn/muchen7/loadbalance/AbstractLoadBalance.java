package cn.muchen7.loadbalance;

import cn.muchen7.utils.MrpcException;

import java.util.List;

/**
 * @author muchen
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public String select(List<String> serviceAddresses) {
        if (serviceAddresses == null || serviceAddresses.size() == 0) {
            throw new MrpcException("服务列表为空,无法找到指定服务");
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses);
    }

    /**
     * 从服务列表中选择一个服务
     *
     * @param serviceAddresses 服务列表
     * @return 根据负载均衡算法返回服务
     */
    protected abstract String doSelect(List<String> serviceAddresses);
}
