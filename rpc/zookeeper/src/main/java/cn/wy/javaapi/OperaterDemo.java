package cn.wy.javaapi;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * Created by 胡歌的小迷弟 on 2020/2/19 10:45
 */
public class OperaterDemo implements Watcher{

    private final static String CONNECTSTRING="182.92.73.88:2181";
    private static CountDownLatch countDownLatch=new CountDownLatch(1);

    private static ZooKeeper zookeeper;
    private static Stat stat=new Stat();
    public static void main(String[] args) {

        try {
            zookeeper = new ZooKeeper(CONNECTSTRING, 5000, new OperaterDemo());
             countDownLatch.await();

            while(!zookeeper.getState().equals("CONNECTED")){
                System.out.println(zookeeper.getState());
                Thread.sleep(2000);
            }
            zookeeper.create("/node1","123".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        }
        catch (Exception e) {
            e.printStackTrace();
        }



    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if(watchedEvent.getState()==Event.KeeperState.SyncConnected){
            countDownLatch.countDown();
            System.out.println("已连接上");
        }else if(watchedEvent.getType()==Event.EventType.NodeCreated){
            try {
                System.out.println("节点创建路径："+watchedEvent.getPath()+"->节点的值："+
                        zookeeper.getData(watchedEvent.getPath(),true,stat));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
