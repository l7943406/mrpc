package cn.muchen7.client;

import cn.muchen7.utils.HashMultiMap;
import cn.muchen7.utils.MrpcException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * 子节点变化监听器
 *
 * @author muchen
 */
public class ZkNodeWatcher implements Watcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkNodeWatcher.class);

    /**
     * 服务列表
     */
    private final HashMultiMap<String, String> servicesMap;

    /**
     * zookeeper实例
     */
    private final ZooKeeper zk;

    public ZkNodeWatcher(HashMultiMap<String, String> servicesMap, ZooKeeper zk) {
        this.servicesMap = servicesMap;
        this.zk = zk;
    }

    @Override
    public void process(WatchedEvent event) {
        // 如果发生变化的在服务器节点下,更新节点信息
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            try {
                String path = event.getPath(),
                        serviceInterfaceName = path.substring(path.lastIndexOf("/") + 1);
                List<String> beanInfos = zk.getChildren(path, this);
                servicesMap.put(serviceInterfaceName, new CopyOnWriteArraySet<>(beanInfos));
                LOGGER.info("注册中心节点{}发生变化，触发消费者服务列表更新...", path);
                LOGGER.info("注册中心节点变化为 : " + beanInfos);
            } catch (Exception e) {
                throw new MrpcException(e);
            }
        }
    }
}
