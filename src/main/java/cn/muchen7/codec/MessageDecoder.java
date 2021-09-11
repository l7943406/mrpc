package cn.muchen7.codec;

import cn.muchen7.message.MrpcRequest;
import cn.muchen7.message.MrpcResponse;
import cn.muchen7.utils.GzipUtil;
import cn.muchen7.utils.ProtoStuffUtil;
import cn.muchen7.utils.ServerType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * netty消息解码器
 *
 * @author muchen
 */
public class MessageDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDecoder.class);

    private final Class<?> clazz;

    /**
     * 根据 客户端类型选取消息解码成什么类
     * 如果是客户端则只会收到response消息
     * 如果是服务端则只会收到request消息
     */
    public MessageDecoder(ServerType type) {
        switch (type) {
            case CLIENT:
                this.clazz = MrpcResponse.class;
                break;
            case SERVER:
                this.clazz = MrpcRequest.class;
                break;
            default:
                this.clazz = Object.class;
                LOGGER.warn("解码器类型未知");
        }
        LOGGER.debug("消息解码器初始化 解码消息类型 : " + clazz);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        LOGGER.debug("解码消息 : " + byteBuf);
        // 一个int的空间用于存储长度信息 如果此信息不存在,不处理此次请求
        int intSize = 4;
        if (byteBuf.readableBytes() < intSize) {
            LOGGER.warn("请求不包含请求长度,丢弃请求 : " + byteBuf);
            return;
        }
        //标记当前readIndex位置
        byteBuf.markReaderIndex();
        //获取一个int的长度 此长度为本次请求的长度信息
        int len = byteBuf.readInt();
        //如果len错误 结束此次请求
        if (len < 0) {
            LOGGER.warn("请求长度信息错误,丢弃本次请求 : " + byteBuf);
            channelHandlerContext.close();
        }
        //读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex. 这个配合markReaderIndex使用的。把readIndex重置到mark的地方
        if (byteBuf.readableBytes() < len) {
            byteBuf.resetReaderIndex();
        }
        // 将ByteBuf转换为byte[]
        byte[] zipData = new byte[len];
        byteBuf.readBytes(zipData);

        // 解压数据
        byte[] data = unCompress(zipData);

        // 将data转换成object
        Object obj = ProtoStuffUtil.deserialize(data, clazz);

        LOGGER.debug("解码并解压消息成功,解码为对象 : " + obj);
        list.add(obj);
    }

    private byte[] unCompress(byte[] bytes) {
        return GzipUtil.unCompress(bytes);
    }
}
