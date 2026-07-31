package cn.wy.factory;

/**
 * Created by 胡歌的小迷弟 on 2020/3/8 16:18
 */
public class MyGirlFriend implements Girl{


    @Override
    public void who(String name) {
        System.out.println(name+"is my girlfriend");
    }
}
