package cn.wy.observer;






import sun.plugin2.jvm.RemoteJVMLauncher;

import java.lang.reflect.Method;

/**
 * Created by 胡歌的小迷弟 on 2020/3/11 15:33
 */
public class Listener extends Event {

    public void addlistener(Object clazz, Observer call, Method method){
       super.clazz=clazz;
       super.call=call;
       super.method=method;
    }


}
