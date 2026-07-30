package cn.wy.distrilock;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by 胡歌的小迷弟 on 2020/2/19 15:49
 */
public class MyDistributeLock {

    private  ZooKeeper zooKeeper;
    private static final String ROOT_LOCKS="/LOCKS";//根节点
    private final static String CONNECTSTRING="182.92.73.88:2181";
    private final static byte[] data={1,2}; //节点的数据
    private CountDownLatch countDownLatch=new CountDownLatch(1);
    private  int sessionTimeout=5000;
    private String lockID; //记录锁节点id
    public MyDistributeLock(){
        this.zooKeeper=getInstance();
    }


    private ZooKeeper getInstance()  {

        final CountDownLatch countDownLatch1=new CountDownLatch(1);
        ZooKeeper zooKeeper= null;
        try {
            zooKeeper = new ZooKeeper(CONNECTSTRING, sessionTimeout, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                   if(event.getState()==Event.KeeperState.SyncConnected){
                       countDownLatch1.countDown();
                   }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return zooKeeper;
    }



    public void lock() throws KeeperException, InterruptedException {
        lockID=zooKeeper.create(ROOT_LOCKS+"/",data, ZooDefs.Ids.
                OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        List<String> childrenNodes=zooKeeper.getChildren(ROOT_LOCKS,true);//获取根节点下的所有子节点
        //排序，从小到大
        SortedSet<String> sortedSet=new TreeSet<String>();
        for(String children:childrenNodes){
            sortedSet.add(ROOT_LOCKS+"/"+children);
        }

        String first=sortedSet.first();
        if(lockID.equals(first)){
            System.out.println(Thread.currentThread().getName()+"->成功获得锁，lock节点为:["+lockID+"]");
            return;
        }

        SortedSet<String> lessThanLockId=sortedSet.headSet(lockID);
        if(!lessThanLockId.isEmpty()){
            String preid=lessThanLockId.last();
            zooKeeper.exists(preid,new MyWatcher(countDownLatch));
            countDownLatch.await(sessionTimeout, TimeUnit.MILLISECONDS);
            System.out.println(Thread.currentThread().getName()+"->成功获得锁，lock节点为:["+lockID+"]");
        }

    }
    public void unlock(){
        //System.out.println(Thread.currentThread().getName()+"->开始释放锁:["+lockID+"]");
        try {
            zooKeeper.delete(lockID,-1);
            //System.out.println("节点["+lockID+"]成功被删除");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        final CountDownLatch latch=new CountDownLatch(10);
        final Random random=new Random();
        for(int i=0;i<10;i++){
           new Thread(new Runnable() {
               @Override
               public void run() {
                   MyDistributeLock lock=null;
                   try {
                       lock=new MyDistributeLock();
                       latch.countDown();
                       latch.await();
                       lock.lock();
                       Thread.sleep(random.nextInt(500));
                   } catch (Exception e) {
                       e.printStackTrace();
                   } finally {
                       if(lock!=null){
                           lock.unlock();
                       }
                   }
               }

           }).start();
        }
    }

}
