package cn.wy.service;

import cn.wy.server.zookeeper.annotation.ServiceAnnotation;

/**
 * Created by 胡歌的小迷弟 on 2020/3/4 10:18
 */

@ServiceAnnotation(Myservice.class)
public class Myserviceimpl implements Myservice {
    @Override
    public String service(String str) {
        return "service1"+str;
    }
}
