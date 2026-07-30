package cn.wy.client.zookeeper.handler;

import cn.wy.client.zookeeper.net.TCPTransport;
import cn.wy.request.RpcRequest;
import cn.wy.client.zookeeper.service.DiscoverService;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by 胡歌的小迷弟 on 2020/3/7 11:42
 */
public class MyHandler implements InvocationHandler {

    private DiscoverService discoverService;
    private String version;

    public MyHandler(DiscoverService discoverService, String version) {
        this.discoverService=discoverService;
        this.version=version;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //组装请求
        RpcRequest request=new RpcRequest();
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setVersion(version);

        String serviceAddress=discoverService.findService(request.getClassName()); //根据接口名称得到对应的服务地址
        //通过tcp传输协议进行传输
        TCPTransport tcpTransport=new TCPTransport(serviceAddress);
        //发送请求
        return tcpTransport.send(request);
    }
}
