package cn.muchen7.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 声明为rpc服务
 *
 * @author muchen
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface MrpcService {

    /**
     * 支持多个接口
     */
    Class<?>[] interfaces();

}
