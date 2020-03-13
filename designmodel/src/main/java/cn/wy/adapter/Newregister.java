package cn.wy.adapter;

/**
 * Created by 胡歌的小迷弟 on 2020/3/11 11:37
 */
public class Newregister extends Oldregister {


    @Override
    public String regist() {

        System.out.println("new");

        return super.regist();
    }
}
