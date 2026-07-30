package cn.wy.single;

/**
 * Created by 胡歌的小迷弟 on 2020/3/10 10:34
 */
public class LazySingle {

    private LazySingle(){}

    LazySingle lazySingle;

    public LazySingle getInstance(){
        lazySingle=new LazySingle();
        return lazySingle;
    }

}
