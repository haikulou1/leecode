package cn.wy.strategy;

/**
 * Created by 胡歌的小迷弟 on 2020/3/11 9:37
 */
public class weiPay implements Pay {
    @Override
    public PayState paymoney() {
        System.out.println("weipay");
        return new PayState("300");
    }
}
