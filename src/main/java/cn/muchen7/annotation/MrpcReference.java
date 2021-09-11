package cn.muchen7.annotation;

import cn.muchen7.loadbalance.LoadBalance;
import cn.muchen7.loadbalance.RandomLoadBalance;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 为该属性注入RPC服务对象
 * @author muchen
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface MrpcReference {
    /**
     * 负载均衡方式
     */
    Class<? extends LoadBalance> loadBalance() default RandomLoadBalance.class;

}
