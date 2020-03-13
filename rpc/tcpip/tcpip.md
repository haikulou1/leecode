## tcp/ip协议
* TCP/IP（Transmission Control Protocol/Internet Protocol）是一种可靠的网络数据传输控制协议。定义了主机如何连入因特网以及数据如何在他们之间传输的标准。

## 三次握手协议
* 所谓三次握手（Three-Way Handshake）即建立TCP连接，就是指建立一个TCP连接时，需要客户端和服务端总共发送3个包以确认连接的建立。

 TCP共有6个标志位，分别是：
 
SYN(synchronous),建立联机。

ACK(acknowledgement),确认。

PSH(push),传输。

FIN(finish),结束。

RST(reset),重置。

URG(urgent),紧急。


![tcp/ip图解](https://img-blog.csdnimg.cn/20200216150528709.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI2NzM3NjY3,size_16,color_FFFFFF,t_70)
（1）第一次握手：Client将标志位SYN（同步序列编号）(Synchronize Sequence Numbers)置为1，随机产生一个值seq=J，并将该数据包发送给Server，Client进入SYN_SENT状态，等待Server确认。
（2）第二次握手：Server收到数据包后由标志位SYN=1知道Client请求建立连接，Server将标志位SYN和ACK都置为1，ack=J+1，随机产生一个值seq=K，并将该数据包发送给Client以确认连接请求，Server进入SYN_RCVD状态。
（3）第三次握手：Client收到确认后，检查ack是否为J+1，ACK是否为1，如果正确则将标志位ACK置为1，ack=K+1，并将该数据包发送给Server，Server检查ack是否为K+1，ACK是否为1，如果正确则连接建立成功，Client和Server进入ESTABLISHED状态，完成三次握手，随后Client与Server之间可以开始传输数据了。

1. 序列号seq
占4个字节，用来标记数据段的顺序，TCP把连接中发送的所有数据字节都编上一个序号，第一个字节的编号由本地随机产生，给字节编上序号后，就给每一个报文段指派一个序号，序列号seq就是这个报文段中的第一个字节的数据编号。

2. 确认号ack
占4个字节，期待收到对方下一个报文段的第一个数据字节的序号，序列号表示报文段携带数据的第一个字节的编号，而确认号指的是期望接受到下一个字节的编号，因此挡墙报文段最后一个字节的编号+1即是确认号。

3. 确认ACK
占1个比特位，仅当ACK=1，确认号字段才有效。ACK=0，确认号无效。

4. 同步SYN
连接建立时用于同步序号。当SYN=1，ACK=0表示：这是一个连接请求报文段。若同意连接，则在响应报文段中使用SYN=1，ACK=1.因此，SYN=1表示这是一个连接请求，或连接接收报文，SYN这个标志位只有在TCP建立连接才会被置为1，握手完成后SYN标志位被置为0.

5. 终止FIN
用来释放一个
## SYN攻击：
  在三次握手过程中，Server发送SYN-ACK之后，收到Client的ACK之前的TCP连接称为半连接（half-open connect），此时Server处于SYN_RCVD状态，当收到ACK后，Server转入ESTABLISHED状态。SYN攻击就是Client在短时间内伪造大量不存在的IP地址，并向Server不断地发送SYN包，Server回复确认包，并等待Client的确认，由于源地址是不存在的，因此，Server需要不断重发直至超时，这些伪造的SYN包将产时间占用未连接队列，导致正常的SYN请求因为队列满而被丢弃，从而引起网络堵塞甚至系统瘫痪。SYN攻击时一种典型的DDOS攻击，检测SYN攻击的方式非常简单，即当Server上有大量半连接状态且源IP地址是随机的，则可以断定遭到SYN攻击了，使用如下命令可以让之现行：
  ```java
    #netstat -nap | grep SYN_RECV
  ```

## 四次挥手
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200216153037417.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI2NzM3NjY3,size_16,color_FFFFFF,t_70)
四次挥手即终止TCP连接，就是指断开一个TCP连接时，需要客户端和服务端总共发送4个包以确认连接的断开。在socket编程中，这一过程由客户端或服务端任一方执行close来触发。

由于TCP连接是全双工的，因此，每个方向都必须要单独进行关闭，这一原则是当一方完成数据发送任务后，发送一个FIN来终止这一方向的连接，收到一个FIN只是意味着这一方向上没有数据流动了，即不会再收到数据了，但是在这个TCP连接上仍然能够发送数据，直到这一方向也发送了FIN。首先进行关闭的一方将执行主动关闭，而另一方则执行被动关闭。

（1） TCP客户端发送一个FIN报文，用来关闭客户到服务器的数据传送。

（2） 服务器收到这个FIN报文，它发回一个ACK报文，确认序号为收到的序号加1。和SYN一样，一个FIN报文将占用一个序号。

（3） 服务器关闭客户端的连接，发送一个FIN给客户端。

（4） 客户端发回ACK报文确认，并将确认序号设置为收到序号加1。

