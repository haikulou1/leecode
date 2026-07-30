package cn.wy.observer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by 胡歌的小迷弟 on 2020/3/11 15:33
 */
public class Event {


    static Object clazz;
    static Observer call;
    static Method method;


    protected void trigger(Object clazz, Observer call, Method method) {

        try {
            method.invoke(clazz,null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    protected void trigger() {
        trigger(clazz,call,method);
    }
}
