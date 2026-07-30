package cn.wy.client.zookeeper.proxy;

import cn.wy.client.zookeeper.handler.MyHandler;
import cn.wy.client.zookeeper.service.DiscoverService;
import cn.wy.service.Myservice;

import java.lang.reflect.Proxy;

/**
 * Created by 胡歌的小迷弟 on 2020/3/7 11:40
 */
public class Myproxy {

    public DiscoverService discoverService;


    public Myproxy(DiscoverService discoverService){
        this.discoverService=discoverService;
    }


    /**
     * 创建客户端的远程代理。通过远程代理进行访问
     * @param interfaceCls
     * @param <T>
     * @return
     */
    public <T> T clientProxy(final Class<T> interfaceCls,String version){
        //使用到了动态代理。
        return (T)Proxy.newProxyInstance(interfaceCls.getClassLoader(),
                new Class[]{interfaceCls},new MyHandler(discoverService,version));
    }

}
