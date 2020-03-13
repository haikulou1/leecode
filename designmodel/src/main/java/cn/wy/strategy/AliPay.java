package cn.wy.strategy;

/**
 * Created by 胡歌的小迷弟 on 2020/3/11 9:36
 */
public class AliPay implements Pay {
    @Override
    public PayState paymoney() {
        System.out.println("ali");
        return new PayState("200");
    }
}
