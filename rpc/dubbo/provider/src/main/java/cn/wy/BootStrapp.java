package cn.wy;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Created by 胡歌的小迷弟 on 2020/3/7 16:13
 */
public class BootStrapp {

    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext classPathXmlApplicationContext
                =new ClassPathXmlApplicationContext("dubbo-server.xml");
        classPathXmlApplicationContext.start();

        System.in.read();
    }
}
