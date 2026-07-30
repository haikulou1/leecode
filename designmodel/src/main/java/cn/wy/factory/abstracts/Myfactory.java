package cn.wy.factory.abstracts;

import cn.wy.factory.abstracts.base.Factory;

/**
 * Created by 胡歌的小迷弟 on 2020/3/10 10:19
 */
public class Myfactory extends Factory {

    @Override
    public void getGirlFriends(String name) {
        System.out.println(name);
    }
}
