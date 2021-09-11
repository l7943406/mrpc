package cn.muchen7.annotation;


import java.lang.annotation.*;

/**
 * 启动mRpc服务
 *
 * @author muchen
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableMrpc {

}