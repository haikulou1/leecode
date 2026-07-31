## 分布式环境

### 分布式环境的特点
1. 分布性
2. 并发性：程序运行过程中，并发性操作是很常见的。比如同一个分布式系统中的多个节点，同时访问一个共享资源。数据库、分布式存储。

3. 无序性：进程之间的消息通信，会出现顺序不一致问题。


### 分布式环境面临的问题
1. 网络通信：网络本身的不可靠性，因此会涉及到一些网络通信问题。
2. 网络分区(脑裂)：当网络发生异常导致分布式系统中部分节点之间的网络延时不断增大，最终导致组成分布式架构的所有节点，只有部分节点能够正常通信。
3. 三态：在分布式架构里面，有成功、失败、超时三种状态。
4. 分布式事务：ACID(原子性、一致性、隔离性、持久性)。

### CAP/BASE理论
#### CAP
1. consistency（一致性）所有节点上的数据保持一致。
2. availablility（可用性）对于每一个操作请求，系统必须能够在指定的时间内返回对应的处理结果。 
+partition-tolerance（分区容错）表示系统出现脑裂以后，可能导致某些server集群中的其他机器失去联系

一个分布式系统不可能同时满足一致性、可用性、分区性容错性三个需求，但是分区容错性必须满足一个最基本的要求。CAP理论仅适用于原子读写Nosql场景，不适用于数据库系统，虽然XA（分布式事务规范）事务会保证数据在分布式系统下的ACID特性，但是会带来性能方面的影响。

### #BASE
1. 基本可用
2. 响应时间点上的损失：允许在出现某种故障的情况下响应时间增加
3. 功能上的损失：允许在高流量的情况下，进行服务降级
4. 软状态 系统在不影响整体可用性的情况下，允许不同节点的数据副本之间同步出现延时。
5. 最终一致性 ：强调系统中所有的数据副本，在经过一段时间的同步后，最终能够达到一个一致的状态。


## zookeeper


### 概念
一套分布式数据一致性解决方案。

### 功能
1. 数据的发布/订阅（配置中心:disconf）  
2.  负载均衡（dubbo利用了zookeeper机制实现负载均衡） 
3. 命名服务
4. master选举(kafka、hadoop、hbase)
5. 分布式队列
6. 分布式锁

### zookeeper的特性
1. 顺序一致性:从同一个客户端发起的事务请求，最终会严格按照顺序被应用到zookeeper中。
2. 原子性：所有的事务请求的处理结果在整个集群中的所有机器上的应用情况是一致的，也就是说，要么整个集群中的所有机器都成功应用了某一事务、要么全都不应用。
3. 可靠性：一旦服务器成功应用了某一个事务数据，并且对客户端做了响应，那么这个数据在整个集群中一定是同步并且保留下来的。
4. 实时性：一旦一个事务被成功应用，客户端就能够立即从服务器端读取到事务变更后的最新数据状态；（zookeeper仅仅保证在一定时间内，近实时）


### zookeeper安装
##### 单机环境安装
```java
1.	下载zookeeper的安装包
http://apache.fayea.com/zookeeper/stable/zookeeper-3.4.10.tar.gz
2.	解压zookeeper 
tar -zxvf zookeeper-3.4.10.tar.gz
3.	cd 到 ZK_HOME/conf  , copy一份zoo.cfg
cp  zoo_sample.cfg  zoo.cfg
4.	sh zkServer.sh
{start|start-foreground|stop|restart|status|upgrade|print-cmd}
5.	sh zkCli.sh -server  ip:port
```

#### 集群环境
zookeeper集群, 包含三种角色: leader / follower /observer
https://my.oschina.net/u/3754001/blog/1802140

## 数据模型
zookeeper的数据模型和文件系统类似，每一个节点称为：znode.  是zookeeper中的最小数据单元。每一个znode上都可以保存数据和挂载子节点。 从而构成一个层次化的属性结构。
### 节点特性
1. 持久化节点  ： 节点创建后会一直存在zookeeper服务器上，直到主动删除
2. 持久化有序节点 ：每个节点都会为它的一级子节点维护一个顺序
3. 临时节点 ： 临时节点的生命周期和客户端的会话保持一致。当客户端会话失效，该节点自动清理
4. 临时有序节点 ： 在临时节点上多了一个顺序性特性

### 会话

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200218113726338.png)

### Watcher
zookeeper提供了分布式数据发布/订阅,zookeeper允许客户端向服务器注册一个watcher监听。当服务器端的节点触发指定事件的时候会触发watcher。服务端会向客户端发送一个事件通知,watcher的通知是一次性，一旦触发一次通知后，该watcher就失效。

### ACL
zookeeper提供控制节点访问权限的功能，用于有效的保证zookeeper中数据的安全性。避免误操作而导致系统出现重大事故。
CREATE /READ/WRITE/DELETE/ADMIN

### zookeeper的命令操作
1. create [-s] [-e] path data acl
-s 表示节点是否有序
-e 表示是否为临时节点
默认情况下，是持久化节点
2. get path [watch]
获得指定 path的信息

3. set path data [version]
修改节点 path对应的data
乐观锁的概念
数据库里面有一个 version 字段去控制数据行的版本号
4. delete path [version]
删除节点

### stat信息
查询命令：stat path

cversion = 0       子节点的版本号
aclVersion = 0     表示acl的版本号，修改节点权限
dataVersion = 1    表示的是当前节点数据的版本号

czxid    节点被创建时的事务ID
mzxid   节点最后一次被更新的事务ID
pzxid    当前节点下的子节点最后一次被修改时的事务ID

ctime = Sat Aug 05 20:48:26 CST 2017
mtime = Sat Aug 05 20:48:50 CST 2017
ephemeralOwner = 0x0   创建临时节点的时候，会有一个sessionId 。 该值存储的就是这个sessionid
dataLength = 3    数据值长度
numChildren = 0  子节点数

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200218114152371.png)







