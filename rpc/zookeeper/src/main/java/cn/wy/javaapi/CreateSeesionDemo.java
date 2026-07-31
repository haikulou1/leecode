package cn.wy.javaapi;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by 胡歌的小迷弟 on 2020/2/19 10:35
 */
public class CreateSeesionDemo {

    private final static String CONNECTSTRING="182.92.73.88:2181";
    private static CountDownLatch countDownLatch=new CountDownLatch(1);

    public static void main(String[] args) {

        try {
            ZooKeeper zooKeeper = new ZooKeeper(CONNECTSTRING, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        countDownLatch.countDown();
                        System.out.println("政委");
                    }
                }
            });
            //控制主线程的执行
        countDownLatch.await();
    }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
