package cn.muchen7.loadbalance;


import java.util.List;

/**
 * @author muchen
 */
public interface LoadBalance {
    /**
     * 从服务列表中选择一个服务
     *
     * @param serviceAddresses 服务列表
     * @return 根据负载均衡算法返回服务
     */
    String select(List<String> serviceAddresses);
}
