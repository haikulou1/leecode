package cn.wy.jdk.DefineProxy;

import java.lang.reflect.Method;

/**
 * Created by 胡歌的小迷弟 on 2019/11/14 15:37
 */
public interface MyInvocationHandler {

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable;

}
