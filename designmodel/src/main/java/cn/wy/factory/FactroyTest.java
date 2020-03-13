package cn.wy.factory;

import cn.wy.factory.abstracts.Myfactory;
import cn.wy.factory.abstracts.base.Factory;
import cn.wy.factory.simple.GirlFriendFacotory;
import org.testng.annotations.Test;


/**
 * Created by 胡歌的小迷弟 on 2020/3/8 16:24
 */
public class FactroyTest {

    public static void main(String[] args) {
        try {
           Girl girl= (Girl) GirlFriendFacotory.getInstance(MyGirlFriend.class);
           girl.who("肖秋菱");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAbst(){
        Factory factory=new Myfactory();
        factory.getGirlFriends("肖秋菱");
    }
}
