package cn.wy.client.zookeeper;

import cn.wy.client.zookeeper.proxy.Myproxy;
import cn.wy.client.zookeeper.service.DiscoverService;
import cn.wy.service.Myservice;

/**
 * Created by 胡歌的小迷弟 on 2020/3/4 10:54
 */
public class RpcZooClient {

    public static void main(String[] args) {
        DiscoverService discoverService=new DiscoverService();

        Myservice myservice= new Myproxy(discoverService).clientProxy(Myservice.class,null);
        System.out.println(myservice.service("wy"));
    }
}
