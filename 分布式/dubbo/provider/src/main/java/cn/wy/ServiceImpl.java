package cn.wy;

/**
 * Created by 胡歌的小迷弟 on 2020/2/6 10:04
 */
public class ServiceImpl implements Service {
    @Override
    public DoOrderResponse say(DoOrderRequest doOrderRequest) {
        System.out.println("曾经来过："+doOrderRequest);
        DoOrderResponse response=new DoOrderResponse();
        response.setCode("1000");
        response.setMemo("处理成功");
        return response;
    }
}
