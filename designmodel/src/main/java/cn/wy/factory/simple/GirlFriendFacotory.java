package cn.wy.factory.simple;

/**
 * Created by 胡歌的小迷弟 on 2020/3/8 16:20
 */
public class GirlFriendFacotory {


    public static Object getInstance(Class clazz) throws IllegalAccessException, InstantiationException {
        return clazz.newInstance();
    }
}
