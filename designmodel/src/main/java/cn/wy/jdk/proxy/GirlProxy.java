package cn.wy.jdk.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by 胡歌的小迷弟 on 2020/3/13 10:29
 */
public class GirlProxy implements InvocationHandler {

    Girl girl;


    public GirlProxy(Girl girl){
        this.girl=girl;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        System.out.println(method.invoke(girl,args));
        return null;
    }
}
