package cn.wy.jdk.proxy;

import java.lang.reflect.Proxy;

/**
 * Created by 胡歌的小迷弟 on 2020/3/13 10:31
 */
public class Test {

    public static void main(String[] args) {
        Girl girl=new GirlImpl();

        Girl girl1= (Girl) Proxy.newProxyInstance(Test.class.getClassLoader(),new Class[]{Girl.class},new GirlProxy(girl));
        girl1.name("aaa");

    }
}
