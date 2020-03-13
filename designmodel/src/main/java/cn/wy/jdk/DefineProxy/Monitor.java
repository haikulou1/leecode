package cn.wy.jdk.DefineProxy;

import java.lang.reflect.Method;

/**
 * Created by 胡歌的小迷弟 on 2019/11/14 16:22
 */
public class Monitor implements MyInvocationHandler {
    private Student target;

    public  Object getInstance(Object student){
        this.target= (Student) student;
        return MyProxy.newProxyInstance(new MyClassLoader(),student.getClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("我也大声朗读");
        return method.invoke(target,args);
    }
}
