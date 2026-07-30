package cn.wy.service;

import cn.wy.server.zookeeper.annotation.ServiceAnnotation;

/**
 * Created by 胡歌的小迷弟 on 2020/3/7 9:47
 */
@ServiceAnnotation(Myservice.class)
public class MyServiceImpl2 implements Myservice {
    @Override
    public String service(String str) {
        return "service2"+str;
    }
}
