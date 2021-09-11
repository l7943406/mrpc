package cn.muchen7.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * gzip 压缩工具类
 * 对于长消息先尝试压缩后再发送出去
 *
 * @author muchen
 */
public class GzipUtil {

    /**
     * 解压缓冲区大小
     */
    private static final Integer GZIP_BUFFER_LENGTH = 512;

    /***
     * 压缩
     * @param bytes 要压缩的字节数组
     * @return 尝试压缩后的字节数组
     * */
    public static byte[] compress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(bytes);
            gzip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 如果内容压缩后长度还变长了 直接返回bytes
        if (out.size() < bytes.length) {
            return out.toByteArray();
        } else {
            return bytes;
        }
    }

    /***
     * 解压缩
     * @param bytes 要解压的字节数组
     * @return 尝试解压后的字节数组
     * */
    public static byte[] unCompress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        //如果不是gzip压缩的数据 直接返回
        if (!isGzipBytes(bytes)) {
            return bytes;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream unGzip = new GZIPInputStream(in);
            byte[] buffer = new byte[GZIP_BUFFER_LENGTH];
            int n;
            while ((n = unGzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    /**
     * 是否压缩判断
     *
     * @param bytes 要判断是否是被gzip压缩的字节数组
     * @return 是否被压缩过
     */
    private static boolean isGzipBytes(byte[] bytes) {
        return bytes[0] == (byte) GZIPInputStream.GZIP_MAGIC
                && bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >>> 8);
    }
}
