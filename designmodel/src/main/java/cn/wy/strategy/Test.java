package cn.wy.strategy;

/**
 * Created by 胡歌的小迷弟 on 2020/3/11 9:38
 */
public class Test {


    /**
     * 策略模式：只有选择权（由用户选择已有的固定算法）
     * 模板模式：侧重点不是选择，你没有选择，你可以参与某一部分内容的自定义
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(PayEnum.ALI_PAY.getPay().paymoney());
    }
}
