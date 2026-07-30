package cn.wy.client.zookeeper.service;

import cn.wy.client.zookeeper.loadbalance.LoadBanalce;
import cn.wy.client.zookeeper.loadbalance.RandomLoadBanalce;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 胡歌的小迷弟 on 2020/3/7 11:20
 */
public class DiscoverService {


    CuratorFramework curatorFramework;
    private static  final String CONNECTSTRING="182.92.73.88";

    public final static String ZK_REGISTER_PATH="/registrys";

    List<String >childs=new ArrayList<>();


    public DiscoverService(){


        curatorFramework=CuratorFrameworkFactory.builder().
                connectString(CONNECTSTRING).
                sessionTimeoutMs(4000).
                retryPolicy(new ExponentialBackoffRetry(1000,
                        10)).build();
        curatorFramework.start();

    }


    public String findService(String servicename){
        String path=ZK_REGISTER_PATH+"/"+servicename;

        try {
            childs=curatorFramework.getChildren().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //动态发现服务节点的变化
        registerWatcher(path);

        //负载均衡机制
        LoadBanalce loadBanalce=new RandomLoadBanalce();

        return loadBanalce.selectHost(childs); //返回调用的服务地址

    }

    private void registerWatcher(final String path) {
        PathChildrenCache childrenCache=new PathChildrenCache
                (curatorFramework,path,true);

        PathChildrenCacheListener pathChildrenCacheListener=new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                childs=curatorFramework.getChildren().forPath(path);
            }
        };
        childrenCache.getListenable().addListener(pathChildrenCacheListener);
        try {
            childrenCache.start();
        } catch (Exception e) {
            throw new RuntimeException("注册PatchChild Watcher 异常"+e);
        }

    }


}
