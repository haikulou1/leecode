package cn.wy.jdk.proxy;

import javax.xml.bind.SchemaOutputResolver;

/**
 * Created by 胡歌的小迷弟 on 2020/3/13 10:28
 */
public class GirlImpl implements Girl {
    @Override
    public String name(String str) {
        return str;
    }
}
