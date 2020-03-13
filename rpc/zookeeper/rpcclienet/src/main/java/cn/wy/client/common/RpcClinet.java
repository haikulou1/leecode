package cn.wy.client.common;

import cn.wy.service.Myservice;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 胡歌的小迷弟 on 2020/3/4 10:20
 * 不带zookeeper注册中心的
 */
public class RpcClinet {




    public static void main(String[] args) {
        Myservice myservice= (Myservice) getService(Myservice.class);
        System.out.println(myservice.service("wy"));
    }

    private static Object getService(final Class myserviceClass) {

        return Proxy.newProxyInstance(myserviceClass.getClassLoader(), new Class[]{myserviceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                Socket socket=new Socket("127.0.0.1",7777);
                String calssname=myserviceClass.getName();
                String methodname=method.getName();

                Class<?>[] parameterTypes=method.getParameterTypes();
                ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeUTF(calssname);
                objectOutputStream.writeUTF(methodname);
                objectOutputStream.writeObject(parameterTypes);
                objectOutputStream.writeObject(args);

                ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());
                Object o=objectInputStream.readObject();
                objectInputStream.close();
                objectOutputStream.close();
                return o;
            }
        });
    }
}
