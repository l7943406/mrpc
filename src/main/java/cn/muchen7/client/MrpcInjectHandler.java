package cn.muchen7.client;

import cn.muchen7.annotation.MrpcReference;
import cn.muchen7.loadbalance.LoadBalance;
import cn.muchen7.message.MrpcRequest;
import cn.muchen7.message.MrpcResponse;
import cn.muchen7.utils.HashMultiMap;
import cn.muchen7.utils.MrpcException;
import cn.muchen7.zk.ZkClient;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * RPC服务注册处理器
 * 为spring bean带RpcReference注解的属性注入代理rpc实例
 *
 * @author muchen
 */
public class MrpcInjectHandler implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(MrpcInjectHandler.class);

    /**
     * zookeeper中注册服务器
     */
    private final ZkClient zkClient;

    private final String root;
    /**
     * 所有服务实例
     */
    private final HashMultiMap<String, String> servicesMap = new HashMultiMap<>();
    /**
     * 从zk订阅节点去重 防止订阅多次zk
     */
    private final Set<String> subscribeServiceSet = new CopyOnWriteArraySet<>();

    public MrpcInjectHandler(ZkClient zkClient, String root) {
        this.zkClient = zkClient;
        this.root = root;
    }

    /**
     * 对Spring Bean中带RpcReference注解的属性赋予代理对象
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();
            Arrays.stream(beanClass.getDeclaredFields()).filter(field -> field.isAnnotationPresent(MrpcReference.class))
                    .forEach(field -> {
                        field.setAccessible(true);
                        try {
                            Class<? extends LoadBalance> loadBalanceClass = field.getAnnotation(MrpcReference.class).loadBalance();
                            LOGGER.debug("获取负载均衡模式 : " + loadBalanceClass);
                            // 1、获取字段类型，只处理接口
                            Class<?> fieldTypeClass = field.getType();
                            if (fieldTypeClass.isInterface()) {
                                // 1、从ZK订阅服务
                                subscribeService(fieldTypeClass.getName());

                                // 2、赋值代理对象
                                field.set(bean, getProxy(fieldTypeClass, loadBalanceClass));
                            }
                        } catch (KeeperException | InterruptedException e) {
                            LOGGER.error("属性{}订阅服务异常！", field.getName(), e);
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            LOGGER.error("属性{}设值异常！", field.getName(), e);
                            throw new RuntimeException(e);
                        }
                    });
        }

    }

    /**
     * 从zk订阅指定服务
     *
     * @param interfaceName 服务接口名
     */
    private void subscribeService(String interfaceName) throws KeeperException, InterruptedException {
        //当接口没有注册watcher才注册
        if (!subscribeServiceSet.contains(interfaceName)) {
            subscribeServiceSet.add(interfaceName);
            ZooKeeper zk = zkClient.getZk();
            String path = root + "/" + interfaceName;
            Watcher watcher = new ZkNodeWatcher(servicesMap, zk);
            Stat stat = zk.exists(path, watcher);
            if (Objects.nonNull(stat)) {
                List<String> beanInfos = zk.getChildren(path, watcher);
                LOGGER.debug("interfaceName : " + beanInfos);
                servicesMap.put(interfaceName, new HashSet<>(beanInfos));
            }
        }

    }

    /**
     * 获取JDK代理对象
     */
    @SuppressWarnings("unchecked")
    private <T> T getProxy(Class<T> clazz, Class<? extends LoadBalance> loadBalanceClass) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, (proxy, method, args) -> {
            // 封装参数
            MrpcRequest request = new MrpcRequest();
            request.setMethodName(method.getName());
            request.setParameterTypes(method.getParameterTypes());
            request.setInterfaceName(clazz.getName());
            request.setParameters(args);

            // 获取随机服务
            String[] sv = getServiceAddress(request.getInterfaceName(), loadBalanceClass).split(":");
            LOGGER.debug("全部服务 : " + Arrays.toString(sv));
            MrpcClientHandler client = new MrpcClientHandler(sv[0], Integer.parseInt(sv[1]));

            // 发起远程调用
            LOGGER.debug("发起调用 : " + request);
            MrpcResponse response = client.send(request);
            if (response.getError() != null) {
                throw response.getError();
            }
            return response.getResult();
        });
    }

    /**
     * 获取一个随机的服务提供地址
     *
     * @param interfaceName 服务提供地址
     * @return 地址
     */
    private String getServiceAddress(String interfaceName, Class<? extends LoadBalance> loadBalanceClass) {
        Set<String> servicesInfo = servicesMap.get(interfaceName);
        try {
            String serviceInfo = loadBalanceClass.getDeclaredConstructor().newInstance().select(new LinkedList<>(servicesInfo));
            return serviceInfo.substring(0, serviceInfo.indexOf("?"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new MrpcException("没有找到" + interfaceName + "的对应服务！");
        }
    }
}
