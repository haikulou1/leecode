package cn.wy;

import cn.wy.dubbo.Service;

/**
 * Created by 胡歌的小迷弟 on 2020/2/6 10:04
 */
public class ServiceImpl implements Service {
    @Override
    public String say(String str) {

        return "hello";
    }
}
