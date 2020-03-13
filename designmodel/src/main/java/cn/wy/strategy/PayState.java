package cn.wy.strategy;

/**
 * Created by 胡歌的小迷弟 on 2020/3/11 9:34
 */
public class PayState {

    public PayState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "PayState{" +
                "state='" + state + '\'' +
                '}';
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    private String state;
}
