package cn.wy.server.zookeeper.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * Created by 胡歌的小迷弟 on 2020/3/6 16:26
 */
public class CuratorUtil {


    private static  final String CONNECTSTRING="182.92.73.88";
    public final static String ZK_REGISTER_PATH="/registrys";
     private static CuratorFramework curatorFramework;

    static {
         curatorFramework= CuratorFrameworkFactory.builder()
                .connectString(CONNECTSTRING).retryPolicy(new ExponentialBackoffRetry(1000,10))
                .build();
        curatorFramework.start();

    }


    public static void register(String servicename,String serviceAddr) throws Exception {
        if(curatorFramework.checkExists().forPath(ZK_REGISTER_PATH+"/"+servicename)==null){
                curatorFramework.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT).forPath(ZK_REGISTER_PATH+"/"+servicename,"0".getBytes());
        }
        String addressPath=ZK_REGISTER_PATH+"/"+servicename+"/"+serviceAddr;
        String rsNode=curatorFramework.create().withMode(CreateMode.EPHEMERAL).
                forPath(addressPath,"0".getBytes());
    }
}
