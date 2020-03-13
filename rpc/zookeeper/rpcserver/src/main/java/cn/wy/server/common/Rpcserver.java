package cn.wy.server.common;

import cn.wy.service.Myservice;
import cn.wy.service.Myserviceimpl;
import com.sun.org.apache.xerces.internal.dom.PSVIAttrNSImpl;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 胡歌的小迷弟 on 2020/3/4 10:02
 */
public class Rpcserver {


    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket=new ServerSocket(7777);

        while (true){
            Socket socket=serverSocket.accept();
            ObjectInputStream inputStream=new ObjectInputStream(socket.getInputStream());
            String calssname= inputStream.readUTF();
            String methodname= inputStream.readUTF();
            Class<?>[]parameterTypes= (Class<?>[]) inputStream.readObject();
            Object o[]= (Object[]) inputStream.readObject();

            Class clazz=null;
            if(calssname.equals(Myservice.class.getName())){
                clazz= Myserviceimpl.class;
            }

            Method method=clazz.getMethod(methodname,parameterTypes);
            Object result=method.invoke(clazz.newInstance(),o);

            ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(result);
            objectOutputStream.flush();

            inputStream.close();
            objectOutputStream.close();
            socket.close();

        }
    }

}
