package cn.wy.distrilock;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

/**
 * Created by 胡歌的小迷弟 on 2020/2/19 16:01
 */
public class MyWatcher implements Watcher {

    CountDownLatch countDownLatch;
    public MyWatcher(CountDownLatch countDownLatch){
        this.countDownLatch=countDownLatch;
    }

    @Override
    public void process(WatchedEvent event) {
        if(event.getType()== Event.EventType.NodeDeleted){
            countDownLatch.countDown();
        }
    }
}
