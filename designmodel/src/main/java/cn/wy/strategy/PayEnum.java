package cn.wy.strategy;

/**
 * Created by 胡歌的小迷弟 on 2020/3/11 9:38
 */
public enum PayEnum {



    ALI_PAY(new AliPay()),
    WE_PAY(new weiPay());

    public Pay getPay() {
        return pay;
    }

    public void setPay(Pay pay) {
        this.pay = pay;
    }

    private Pay pay;


      PayEnum(Pay pay){

          this.pay=pay;
      }

}
