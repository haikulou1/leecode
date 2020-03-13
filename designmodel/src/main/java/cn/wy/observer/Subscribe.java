package cn.wy.observer;

import java.lang.reflect.Method;

/**
 * Created by 胡歌的小迷弟 on 2020/3/11 15:34
 */
public class Subscribe extends Event{


    public static void main(String[] args) throws NoSuchMethodException {
        Listener listener=new Listener();
        Method obv = Observer.class.getMethod("obv", null);
        listener.addlistener(new Observer(),new Observer(),obv);

        new Subscribe().sub();
    }


    public   void sub(){
        System.out.println("sub");
        super.trigger();
    }
}
