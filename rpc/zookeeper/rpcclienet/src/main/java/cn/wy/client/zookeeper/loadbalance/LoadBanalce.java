package cn.wy.client.zookeeper.loadbalance;

import java.util.List;


public interface LoadBanalce {

    String selectHost(List<String> repos);
}


