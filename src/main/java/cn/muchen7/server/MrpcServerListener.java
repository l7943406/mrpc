package cn.muchen7.server;

import ch.qos.logback.core.net.server.ServerListener;
import cn.muchen7.codec.MessageDecoder;
import cn.muchen7.codec.MessageEncoder;
import cn.muchen7.utils.MrpcException;
import cn.muchen7.utils.ServerType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 启动Netty监听服务
 *
 * @author muchen
 */
public class MrpcServerListener extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerListener.class);

    /**
     * 服务地址
     */
    private final String serviceIp;
    /**
     * 服务端口
     */
    private final int servicePort;
    /**
     * rpc服务
     */
    private final Map<String, Object> handlers;

    private MrpcServerListener(String serviceIp, int servicePort, Map<String, Object> handlers) {
        this.serviceIp = serviceIp;
        this.servicePort = servicePort;
        this.handlers = handlers;
    }

    public static MrpcServerListener create(String serviceIp, int servicePort, Map<String, Object> handlers) {
        return new MrpcServerListener(serviceIp, servicePort, handlers);
    }

    @Override
    public void run() {
        // 启动Netty服务
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(workerGroup, bossGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) {
                            // 添加编码、解码、业务处理的handler
                            channel.pipeline().addLast(new MessageDecoder(ServerType.SERVER))
                                    .addLast(new MessageEncoder())
                                    .addLast(new MrpcServerHandler(handlers));
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String host = serviceIp;
            int port = servicePort;
            ChannelFuture future = bootstrap.bind(host, port).sync();
            LOGGER.info("server started on {}", host + ":" + port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new MrpcException(e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
