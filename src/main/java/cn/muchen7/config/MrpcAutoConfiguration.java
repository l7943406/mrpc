package cn.muchen7.config;

import cn.muchen7.annotation.EnableMrpc;
import cn.muchen7.client.MrpcInjectHandler;
import cn.muchen7.server.MrpcServer;
import cn.muchen7.zk.ZkClient;
import cn.muchen7.zk.ZkServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author muchen
 */
@Configuration
@ConditionalOnBean(annotation = EnableMrpc.class)
@EnableConfigurationProperties(MrpcProperties.class)
public class MrpcAutoConfiguration {

    private MrpcProperties properties;

    /**
     * 服务消费者的zk客户端
     */
    @Bean
    public ZkClient zkClient() {
        return new ZkClient(properties.getZkServer(), properties.getZkTimeout(), properties.getRoot());
    }

    /**
     * RPC实例注入处理器
     */
    @Bean
    @ConditionalOnBean(ZkClient.class)
    public MrpcInjectHandler rpcInjectHandler(ZkClient client) {
        return new MrpcInjectHandler(client, properties.getRoot());
    }


    /**
     * 服务提供者的zk客户端
     *
     * @return zk客户端
     */
    @Bean
    public ZkServer zkServer() {
        return new ZkServer(properties.getZkServer(), properties.getZkTimeout(), properties.getRoot());
    }

    /**
     * RPC实例注册服务
     *
     * @return RpcServe
     */
    @Bean
    @ConditionalOnBean(ZkServer.class)
    public MrpcServer rpcServer(ZkServer zkServer) {
        return new MrpcServer(properties.getServerIp(), properties.getServerPort(), zkServer, properties.getRoot());
    }

    @Autowired
    public void setProperties(MrpcProperties properties) {
        this.properties = properties;
    }
}
