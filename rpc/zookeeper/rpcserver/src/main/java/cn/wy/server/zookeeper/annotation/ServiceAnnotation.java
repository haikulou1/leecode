package cn.wy.server.zookeeper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by 胡歌的小迷弟 on 2020/3/7 9:40
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceAnnotation {
    Class<?> value();

    String version() default "";
}
