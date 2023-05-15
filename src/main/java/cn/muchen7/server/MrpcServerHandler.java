package cn.muchen7.server;

import cn.muchen7.message.MrpcRequest;
import cn.muchen7.message.MrpcResponse;
import cn.muchen7.utils.SpringUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Map;


/**
 * RPC请求服务处理器
 * 读取请求，反射调用本地方法，返回结果
 *
 * @author muchen
 */
public class MrpcServerHandler extends SimpleChannelInboundHandler<MrpcRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MrpcServerHandler.class);

    /**
     * 服务类对象map key:interfaceName value:object
     */
    private final Map<String, Object> handlerMap;
    private final MeterRegistry registry;

    public MrpcServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
        this.registry = SpringUtil.getBean(MeterRegistry.class);
    }

    /**
     * 接收消息，处理消息，返回结果
     */
    @Override
    public void channelRead0(final ChannelHandlerContext ctx, MrpcRequest request) {
        LOGGER.debug("服务接收到request消息" + request);
        //处理请求
        MrpcResponse response = new MrpcResponse();
        try {
            // 根据request来处理具体的业务调用
            Object result = handle(request);
            this.registry.counter("mrpc.service.counter",
                    "to_method",request.getMethodName(),
                    "to_class",request.getInterfaceName(),
                    "from_client_ip", ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress(),
                    "status","success",
                    "message", "success"
            ).increment();
            response.setResult(result);
        } catch (Throwable t) {
            this.registry.counter("mrpc.service.counter",
                    "to_method",request.getMethodName(),
                    "to_class",request.getInterfaceName(),
                    "from_client_ip", ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress(),
                    "status","failed",
                    "message", t.getLocalizedMessage()
            ).increment();
            response.setError(t);
        }
        LOGGER.debug("处理request消息 完成 返回消息 : " + response);
        //返回response
        // 写入 out，由RpcEncoder进行下一步编码处理，后发送到channel中给客户端
        ctx.writeAndFlush(response)
                .addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 根据request来处理具体的业务调用 调用是通过反射的方式来完成
     */
    private Object handle(MrpcRequest request) throws Throwable {
        // 查找服务类对象
        String interfaceName = request.getInterfaceName();
        Object serviceBean = handlerMap.get(interfaceName);

        // 拿到接口
        Class<?> forName = Class.forName(interfaceName);

        // 拿到要调用的方法名、参数类型、参数值
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Method method = forName.getMethod(methodName, parameterTypes);

        // 调用实现类对象的指定方法并返回结果
        Object[] parameters = request.getParameters();
        return method.invoke(serviceBean, parameters);
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("server caught exception", cause);
        ctx.close();
    }
}
