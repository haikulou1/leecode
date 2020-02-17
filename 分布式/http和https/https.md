## HTTPS
### HTTPS概念
HTTPS 协议是由SSL(Secure Sockets Layer 安全套接层)+HTTP 协议构建的可进行加密传输、身份认证的网络协议，要比http 协议安全。
### https 与http 区别
HTTPS 和HTTP 的区别主要如下：
1）https 协议需要到ca 申请证书，一般免费证书较少，因而需要一定费用。
2）http 是超文本传输协议，信息是明文传输，https 则是具有安全性的ssl加密传输协议。
3）http 和https 使用的是完全不同的连接方式，用的端口也不一样，前者是80，后者是443。
4）http 的连接很简单，是无状态的；HTTPS 协议是由SSL+HTTP 协议构建的可进行加密传输、身份认证的网络协议，比http 协议安全。
1. HTTP 的URL 以http:// 开头， 而HTTPS 的URL 以https://开头
2. HTTP 是不安全的， 而HTTPS 是安全的
3. 在OSI 网络模型中， HTTP 工作于应用层， 而HTTPS 工作在传输层
4. HTTP 无需加密， 而HTTPS 对传输的数据进行加密
5. HTTP 无需证书， 而HTTPS 需要认证证书

### https 的通信过程
![在这里插入图片描述](https://img-blog.csdnimg.cn/2020021715221645.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI2NzM3NjY3,size_16,color_FFFFFF,t_70)
客户端A 和服务器B 之间的交互
1. A 与B 通过TCP 建立链接，初始化SSL 层。
2. 进行SSL 握手，A 发送https 请求，传送客户端SSL 协议版本号、支持的加密算法、
随机数等。
3. 服务器B 把CA 证书（包含B 的公钥），把自己支持的加密算法、随机数等回传给
A。
4. A 接收到CA 证书，验证证书有效性。
5. 校验通过，客户端随机产生一个字符串作为与B 通信的对称密钥，通过CA 证书解
出服务器B 的公钥，对其加密，发送给服务器。
6. B 用私钥解开信息，得到随机的字符串（对称密钥），利用这个密钥作为之后的通
信密钥。
7. 客户端向服务器发出信息，指明后面的数据使用该对称密钥进行加密，同时通知服
务器SSL 握手结束。
8. 服务器接收到信息，使用对称密钥通信，通知握手接收。
9. SSL 握手结束，使用对称密钥加密数据。