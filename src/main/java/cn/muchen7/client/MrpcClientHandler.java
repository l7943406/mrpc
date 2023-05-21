package cn.muchen7.client;

import cn.muchen7.codec.MessageDecoder;
import cn.muchen7.codec.MessageEncoder;
import cn.muchen7.message.MrpcRequest;
import cn.muchen7.message.MrpcResponse;
import cn.muchen7.utils.MrpcException;
import cn.muchen7.utils.ServerType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author muchen
 */
public class MrpcClientHandler extends SimpleChannelInboundHandler<MrpcResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MrpcClientHandler.class);

    private static final EventLoopGroup group = new NioEventLoopGroup();

    /**
     * 服务对象地址
     */
    private final String host;
    /**
     * 服务对象端口
     */
    private final int port;
    /**
     * 用于阻塞连接关闭，等待结果返回
     */
    private final CountDownLatch latch = new CountDownLatch(1);
    /**
     * 响应结果
     */
    private MrpcResponse response;


    public MrpcClientHandler(String host, int port) {
        this.host = host;
        this.port = port;
        LOGGER.debug("创建 MrpcClientHandler " + "\n host : " + host + ":" + port);
    }

    /**
     * 链接服务端，发送消息
     *
     * @param request 请求体
     * @return response
     */
    public MrpcResponse send(MrpcRequest request) {
        LOGGER.debug("发送请求 : " + request);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(MrpcClientHandler.group).channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) {
                    // 添加编码、解码、业务处理的handler
                    ch.pipeline()
                            .addLast(new MessageEncoder())
                            .addLast(new MessageDecoder(ServerType.CLIENT))
                            .addLast(MrpcClientHandler.this);
                    LOGGER.debug("添加编码、解码、业务处理的handler : " + ch);
                }

            }).option(ChannelOption.SO_KEEPALIVE, true);


            // 链接服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            LOGGER.debug("链接服务器" + future);

            // 将request对象写入out bundle处理后发出（即RpcEncoder编码器）
            future.channel().writeAndFlush(request).sync();
            LOGGER.debug("将request对象写入out bundle处理后发出（即RpcEncoder编码器）");

            // 先在此阻塞，等待获取到服务端的返回后，被唤醒，从而关闭网络连接

            latch.await();

            if (response != null) {
                future.channel().closeFuture().sync();
            }
            LOGGER.debug("返回结果 : " + response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new MrpcException("服务调用出现异常");
        }
    }

    /**
     * 读取服务端的返回结果
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, MrpcResponse response) {
        LOGGER.debug("读取服务端的返回结果" + response);
        this.response = response;
        latch.countDown();
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("client caught exception", cause);
        ctx.close();
    }

}
