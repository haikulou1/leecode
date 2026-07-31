package cn.wy.jdk.DefineProxy;

/**
 * Created by 胡歌的小迷弟 on 2019/11/14 16:24
 */
public class Test {

    public static void main(String[] args) {

        Student student= (Student) new Monitor().getInstance(new Xiaoming());

        student.read();
    }
}
