package cn.wy.single;

/**
 * Created by 胡歌的小迷弟 on 2020/3/10 10:35
 */
public class HungrySingle {


    private static  final  HungrySingle HUNGRY_SINGLE=new HungrySingle();


    public static HungrySingle getInstance(){
        return HUNGRY_SINGLE;
    }
}
