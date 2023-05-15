package cn.muchen7.zk;

import cn.muchen7.utils.MrpcException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务消费者的zk客户端
 *
 * @author muchen
 */
public class ZkClient extends ZkInit {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkClient.class);

    /**
     * zk服务
     */
    private ZooKeeper zk;

    public ZkClient(String zkServer, int timeout, String root) {
        init(zkServer, timeout, root);
    }

    /**
     * 连接ZK
     */
    private void init(String zkServer, int timeout, String root) {
        try {
            zk = super.create(zkServer, timeout);
            Stat stat = zk.exists(root, false);
            if (stat == null) {
                // 创建根节点
                zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            LOGGER.error("初始化ZK服务异常！", e);
            throw new MrpcException(e);
        }
    }

    /**
     * 获取zk
     */
    public ZooKeeper getZk() {
        return zk;
    }

}
