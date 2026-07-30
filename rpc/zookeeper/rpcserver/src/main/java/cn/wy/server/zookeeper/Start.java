package cn.wy.server.zookeeper;

import cn.wy.service.MyServiceImpl2;
import cn.wy.service.Myservice;
import cn.wy.service.Myserviceimpl;

import java.io.IOException;

/**
 * Created by 胡歌的小迷弟 on 2020/3/6 16:45
 */
public class Start {

    public static void main(String[] args) throws IOException {

        Myservice myservice1=new Myserviceimpl();
        Myservice myService2=new MyServiceImpl2();

        RpcZooServer rpcZooServer=new RpcZooServer("127.0.0.1:7777");
        rpcZooServer.bind(myservice1,myService2);
        rpcZooServer.publish();

        System.in.read();
    }
}
