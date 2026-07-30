package cn.wy.server.zookeeper.handler;

import cn.wy.request.RpcRequest;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by 胡歌的小迷弟 on 2020/3/7 10:11
 */
public class ProcessorHandler implements Runnable{


    private Socket socket;
    private HashMap<String, Object> map;

    public ProcessorHandler(Socket socket, HashMap<String, Object> map) {
            this.socket=socket;
            this.map=map;
    }

    @Override
    public void run() {
        //处理请求
        ObjectInputStream inputStream=null;
        try {
            //获取客户端的输入流
            inputStream=new ObjectInputStream(socket.getInputStream());
            //反序列化远程传输的对象RpcRequest
            RpcRequest request=(RpcRequest) inputStream.readObject();
            Object result=invoke(request); //通过反射去调用本地的方法

            //通过输出流讲结果输出给客户端
            ObjectOutputStream outputStream=new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(result);
            outputStream.flush();
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Object invoke(RpcRequest rpcRequest) throws Exception {
        Object os[]=rpcRequest.getParameters();
        Class<?>[] clazz=new Class[os.length];

        for(int i=0;i<os.length;i++){
            clazz[i]=os[i].getClass();
        }

        String version=rpcRequest.getVersion();
        String servicename=rpcRequest.getClassName();
        if(!StringUtils.isEmpty(version)){
            servicename+=version;
        }

        String method=rpcRequest.getMethodName();
        Object o=map.get(servicename);
        Method method1=o.getClass().getMethod(method,clazz);
        return  method1.invoke(o,os);
    }
}
