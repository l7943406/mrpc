package cn.muchen7.server;

import cn.muchen7.annotation.MrpcService;
import cn.muchen7.utils.MrpcException;
import cn.muchen7.zk.ZkServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * RPC实例注册服务
 *
 * @author muchen
 */
public class MrpcServer implements ApplicationContextAware, InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(MrpcServer.class);

    /**
     * rpc服务
     */
    private final Map<String, Object> handlers = new HashMap<>();

    /**
     * rpc服务IP
     */
    private final String serviceIp;

    /**
     * rpc服务PORT
     */
    private final int servicePort;

    /**
     * zookeeper服务
     */
    private final ZkServer zkServer;

    private final String root;

    public MrpcServer(String serviceIp, int servicePort, ZkServer zkServer, String root) {
        LOGGER.debug("创建 MrpcServer 服务 ip : " + serviceIp + "\n port : " + servicePort + "\nzkAddress : " + zkServer + "\n + root : " + root);
        this.serviceIp = serviceIp;
        this.servicePort = servicePort;
        this.zkServer = zkServer;
        this.root = root;
    }

    /**
     * 加载所有RPC服务 Spring容器会在加载完后调用
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取所有带 MrpcService 注解的类
        Map<String, Object> rpcServices = applicationContext.getBeansWithAnnotation(MrpcService.class);
        // 将类注册到handlers中
        if (rpcServices.size() != 0) {
            for (Object rpcServiceBean : rpcServices.values()) {
                // 从业务实现类上的自定义注解中获取到value，然后获取到业务接口的全名
                Class<?>[] interfaces = rpcServiceBean.getClass().getAnnotation(MrpcService.class).interfaces();
                for (Class<?> interfaceClass : interfaces) {
                    // 业务接口名
                    String interfaceName = interfaceClass.getName();
                    // 本地存根
                    handlers.put(interfaceName, rpcServiceBean);
                    // 注册到ZK
                    registerToZk(interfaceName);
                    LOGGER.info("服务实例{}注册成功！", rpcServiceBean.getClass().getName());
                }
            }
        }
    }

    /**
     * 启动Netty服务 在setApplicationContext之后被Spring执行
     */
    @Override
    public void afterPropertiesSet() {
        LOGGER.debug("扫描MrpcServer完成 启动netty服务");
        MrpcServerListener.create("0.0.0.0", servicePort, handlers).start();
    }

    /**
     * 注册rpc对象信息到zk
     */
    private void registerToZk(String interfaceName) {
        try {
            // 获取zookeeper连接
            ZooKeeper zk = zkServer.getZk();
            // 1、创建服务接口节点,永久节点

            String interfaceNode = root + "/" + interfaceName;
            Stat stat = zk.exists(interfaceNode, false);
            LOGGER.debug("创建永久节点 : " + interfaceNode);

            if (Objects.isNull(stat)) {
                zk.create(interfaceNode, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            // 2、创建服务对象节点，临时节点
            String beanNode = interfaceNode + "/" + serviceIp + ":" + servicePort + "?" + System.currentTimeMillis();
            zk.create(beanNode, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            LOGGER.debug("创建临时节点 : " + beanNode);

        } catch (Exception e) {
            throw new MrpcException("注册服务到ZK异常！", e);
        }
    }
}