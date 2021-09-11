package cn.muchen7.config;


import cn.muchen7.utils.IpUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author muchen
 */

@ConfigurationProperties(prefix = "mrpc")
public class MrpcProperties {

    /**
     * zookeeper服务地址
     */
    private String zkServer = "127.0.0.1:2181";

    /**
     * 注册中心（ZooKeeper）连接超时时间
     */
    private int zkTimeout = 10000;

    /**
     * 服务root
     */
    private String root = "/mrpc";

    /**
     * rpc服务ip 默认:LocalAddress
     */
    private String serverIp = IpUtil.getLocalAddress();

    /**
     * rpc服务port 默认:3131
     */
    private Integer serverPort = 3131;

    public String getZkServer() {
        return zkServer;
    }

    public void setZkServer(String zkServer) {
        this.zkServer = zkServer;
    }

    public int getZkTimeout() {
        return zkTimeout;
    }

    public void setZkTimeout(int zkTimeout) {
        this.zkTimeout = zkTimeout;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }
}
