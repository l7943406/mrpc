package cn.muchen7.zk;

import cn.muchen7.utils.MrpcException;
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

            // 查看服务器根节点是否存在
            Stat stat = zk.exists(root, false);

            if (stat == null) {
                // 失败
                throw new MrpcException("RPC服务根节点不存在！");
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
