package cn.muchen7.codec;

import cn.muchen7.utils.GzipUtil;
import cn.muchen7.utils.ProtoStuffUtil;
import cn.muchen7.utils.SpringUtil;
import com.google.common.util.concurrent.AtomicDouble;
import io.micrometer.core.instrument.MeterRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * netty消息编码器 序列化并将长度信息写在前4位
 *
 * @author muchen
 */
public class MessageEncoder extends MessageToByteEncoder<Object> {
    private static final AtomicDouble compressibility = new AtomicDouble();
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) {
        //序列化
        byte[] data = ProtoStuffUtil.serialize(o);
        //压缩
        byte[] zipData = compress(data);
        byteBuf.writeInt(zipData.length);
        byteBuf.writeBytes(zipData);
        LOGGER.debug("编码并压缩消息 : length : " + zipData.length + " | " + "{message}");
    }

    private byte[] compress(byte[] bytes) {
        MeterRegistry registry = SpringUtil.getBean(MeterRegistry.class);
        byte[] zipData = GzipUtil.compress(bytes);
        MessageEncoder.compressibility.set(zipData.length * 1.0/bytes.length);
        registry.gauge("mrpc.gzip.compressibility", MessageEncoder.compressibility);
        LOGGER.debug(System.out.format("该次请求压缩前大小 : %d , 压缩后大小 : %d , 压缩率 : %f \n" ,bytes.length, zipData.length,zipData.length * 1.0/bytes.length).toString());
        return zipData;
    }
}
