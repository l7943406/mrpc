package cn.muchen7.zk;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * @author muchen
 */
public class ZkInit {
    /**
     * 连接ZK
     */
    public ZooKeeper create(String zkServer, int timeout) throws Exception {
        CountDownLatch connectedSignal = new CountDownLatch(1);
        ZooKeeper zk = new ZooKeeper(zkServer, timeout, event -> {
            // 建立连接
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                connectedSignal.countDown();
            }
        });
        // 等待连接建立完毕
        connectedSignal.await();
        return zk;
    }
}
