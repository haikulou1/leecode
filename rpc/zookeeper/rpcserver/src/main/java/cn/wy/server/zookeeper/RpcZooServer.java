package cn.wy.server.zookeeper;

import cn.wy.server.zookeeper.annotation.ServiceAnnotation;
import cn.wy.server.zookeeper.handler.ProcessorHandler;
import cn.wy.server.zookeeper.utils.CuratorUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 胡歌的小迷弟 on 2020/3/6 16:23
 */
public class RpcZooServer {

    private  String url;
    //创建一个线程池
    private static final ExecutorService executorService= Executors.newCachedThreadPool();
    private HashMap<String,Object> map=new HashMap<>();

    public  RpcZooServer(String url){
        this.url=url;
    }

    public void bind(Object ... objects){

        for(Object o:objects){
            ServiceAnnotation annotation=o.getClass().getAnnotation(ServiceAnnotation.class);

            String servicename=annotation.value().getName();
            String version=annotation.version();

            if(!StringUtils.isEmpty(version)){
                servicename+=version;
            }

            map.put(servicename,o);
        }
    }


    public void publish(){
        ServerSocket serverSocket=null;

        try {
            String addr[]=url.split(":");
            serverSocket=new ServerSocket(Integer.parseInt(addr[1]));
            for(String sevicename:map.keySet()){
                CuratorUtil.register(sevicename,url);
            }

            while(true){ //循环监听
                Socket socket=serverSocket.accept(); //监听服务
                //通过线程池去处理请求
                executorService.execute(new ProcessorHandler(socket,map));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(serverSocket!=null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
