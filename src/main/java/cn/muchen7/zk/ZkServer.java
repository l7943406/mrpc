package cn.muchen7.zk;

import cn.muchen7.utils.MrpcException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/*** 服务提供者的zk
 * @author muchen
 */
public class ZkServer extends ZkInit {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkServer.class);

    /**
     * zk服务
     */
    private ZooKeeper zk;

    public ZkServer(String zkServer, int timeout, String root) {
        initZk(zkServer, timeout, root);
    }

    /**
     * 初始化ZK
     **/
    private void initZk(String zkServer, int timeout, String zkServerRoot) {
        try {
            zk = super.create(zkServer, timeout);

            Stat stat = zk.exists(zkServerRoot, false);
            if (Objects.isNull(stat)) {
                // 创建根节点
                zk.create(zkServerRoot, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            LOGGER.error("初始化ZK服务异常！", e);
            throw new MrpcException(e);
        }
    }

    /**
     * 获取ZooKeeper
     *
     * @return zookeeper
     */
    public ZooKeeper getZk() {
        return zk;
    }
}
