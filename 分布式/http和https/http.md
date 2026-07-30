# Http
## http概念
超文本传输协议（英文：HyperText Transfer Protocol，缩写：HTTP）是一种用于分布式、协作式和超媒体信息系统的应用层协议。HTTP是万维网的数据通信的基础。
HTTP是一个客户端终端（用户）和服务器端（网站）请求和应答的标准（TCP）。通过使用网页浏览器、网络爬虫或者其它的工具，客户端发起一个HTTP请求到服务器上指定端口（默认端口为80）。我们称这个客户端为用户代理程序（user agent）。应答的服务器上存储着一些资源，比如HTML文件和图像。我们称这个应答服务器为源服务器（origin server）。在用户代理和源服务器中间可能存在多个“中间层”，比如代理服务器、网关或者隧道（tunnel）。


## http 请求报文& http 响应报文
### 请求报文
http请求报文由<font color=red>请求行，请求头，空行，请求数据</font>组成。
1.请求行:url，get/post方法，http版本
2.请求头：关键字和值组成
3.空行
4.请求数据:body

### 响应报文
http响应报文由<font color=red>状态码，HTTP 头部，空行，主体(Body)</font>组成。
1.状态码(Status Code)：描述了响应的状态。可以用来检查是否成功的完成了请求。请求失
败的情况下，状态码可用来找出失败的原因。如果Servlet 没有返回状态码，默认会返回成
功的状态码HttpServletResponse.SC_OK。
2.HTTP 头部(HTTP Header)：它们包含了更多关于响应的信息。比如：头部可以指定认为
响应过期的过期日期，或者是指定用来给用户安全的传输实体内容的编码格式。如何在
Serlet 中检索HTTP 的头部看这里。
3.空行
4.主体(Body)：它包含了响应的内容。它可以包含HTML 代码，图片，等等。主体是由传输
在HTTP 消息中紧跟在头部后面的数据字节组成的。

## http请求方法
1. OPTIONS:返回服务器针对特定资源所支持的HTTP请求方法，也可以利用向web服务器发送“*”的请求来测试服务器的功能性。
2. HEAD：像服务器发送与GET请求相一致的响应，只不过响应体将不会返回，这个方法可以在不必传输整个响应内容的情况下，就可以获取包含在响应消息头中的元信息。
3. GET：向特定的资源发出请求。
4. POST：向指定资源提交数据。
5. PUT:向指定资源位置上传数据。
6. DELETE：请求服务器删除指定的资源。
7. TRACE：回显服务器收到的请求，主要用于测试或诊断
8. CONNECT：HTTP/1.1协议中预留给能够将连接改为管道方式的代理服务器。

## http 请求过程
以下是HTTP 请求/响应的步骤：
1、客户端连接到Web 服务器
一个HTTP 客户端，通常是浏览器，与Web 服务器的HTTP 端口（默认为80）建立一个TCP 套接字连接。
2、发送HTTP 请求
通过TCP 套接字，客户端向Web 服务器发送一个文本的请求报文，一个请求报文由请求行、请求头部、空行和请求数据4 部分组成。
3、服务器接受请求并返回HTTP 响应
Web 服务器解析请求，定位请求资源。服务器将资源复本写到TCP 套接字，由客户端读取。一个响应由状态行、响应头部、空行和响应数据4 部分组成。
4、释放连接TCP 连接
若connection 模式为close，则服务器主动关闭TCP 连接，客户端被动关闭连接，释放TCP 连接;若connection 模式为keepalive，则该连接会保持一段时间，在该时间内可以继续接收请求;
5、客户端浏览器解析HTML 内容
客户端浏览器首先解析状态行，查看表明请求是否成功的状态代码。然后解析每一个响应头，响应头告知以下为若干字节的HTML 文档和文档的字符集。客户端浏览器读取响应数据HTML，根据HTML 的语法对其进行格式化，并在浏览器窗口中显示。

## HTTP状态码
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200217110853640.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI2NzM3NjY3,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200217110900160.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI2NzM3NjY3,size_16,color_FFFFFF,t_70)
### 3XX重定向状态码
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200217110928959.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI2NzM3NjY3,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200217111016625.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI2NzM3NjY3,size_16,color_FFFFFF,t_70)
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200217111023940.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI2NzM3NjY3,size_16,color_FFFFFF,t_70)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200217111030799.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI2NzM3NjY3,size_16,color_FFFFFF,t_70)

## http 长连接短连接
HTTP 协议是无状态
无状态：HTTP 协议是无状态的，指的是协议对于事务处理没有记忆能力，服务器不知道客户端是什么状态。也就是说，打开一个服务器上的网页和上一次打开这个服务器上的网页之间没有任何联系。HTTP 是一个无状态的面向连接的协议，无状态不代表HTTP 不能保持TCP 连接，更不能代表HTTP 使用的是UDP 协议（无连接）。
https://www.cnblogs.com/gotodsp/p/6366163.html
短连接：在HTTP/1.0 中默认使用短连接。也就是说，客户端和服务器每进行一次HTTP操作，就建立一次连接，任务结束就中断连接。当客户端浏览器访问的某个HTML 或其他类型的Web 页中包含有其他的Web 资源（如JavaScript 文件、图像文件、CSS 文件等），每遇到这样一个Web 资源，浏览器就会重新建立一个HTTP 会话。
长连接：而从HTTP/1.1 起，默认使用长连接，用以保持连接特性。使用长连接的HTTP协议，会在响应头加入这行代码：
Connection:keep-alive
在使用长连接的情况下，当一个网页打开完成后，客户端和服务器之间用于传输HTTP 数据的TCP 连接不会关闭，客户端再次访问这个服务器时，会继续使用这一条已经建立的连接。Keep-Alive 不会永久保持连接，它有一个保持时间，可以在不同的服务器软件（如Apache）中设定这个时间。实现长连接需要客户端和服务端都支持长连接。HTTP 协议的长连接和短连接，实质上是TCP 协议的长连接和短连接。

### 什么时候用长连接，短连接？
长连接多用于操作频繁，点对点的通讯，而且连接数不能太多情况，。每个TCP 连接都需要三步握手，这需要时间，如果每个操作都是先连接，再操作的话那么处理速度会降低很多，所以每个操作完后都不断开，次处理时直接发送数据包就OK 了，不用建立TCP 连接。例如：数据库的连接用长连接， 如果用短连接频繁的通信会造成socket 错误，而且频繁的socket创建也是对资源的浪费。而像WEB 网站的http 服务一般都用短链接，因为长连接对于服务端来说会耗费一定的资源，而像WEB 网站这么频繁的成千上万甚至上亿客户端的连接用短连接会更省一些资源，如果用长连接，而且同时有成千上万的用户，如果每个用户都占用一个连接的话，那可想而知吧。所以并发量大，但每个用户无需频繁操作情况下需用短连接好。

### HTTP1.0和HTTP1.1的一些区别

HTTP1.0最早在网页中使用是在1996年，那个时候只是使用一些较为简单的网页上和网络请求上，而HTTP1.1则在1999年才开始广泛应用于现在的各大浏览器网络请求中，同时HTTP1.1也是当前使用最为广泛的HTTP协议。 主要区别主要体现在：

1. 缓存处理，在HTTP1.0中主要使用header里的If-Modified-Since,Expires来做为缓存判断的标准，HTTP1.1则引入了更多的缓存控制策略例如Entity tag，If-Unmodified-Since, If-Match, If-None-Match等更多可供选择的缓存头来控制缓存策略。



2. 带宽优化及网络连接的使用，HTTP1.0中，存在一些浪费带宽的现象，例如客户端只是需要某个对象的一部分，而服务器却将整个对象送过来了，并且不支持断点续传功能，HTTP1.1则在请求头引入了range头域，它允许只请求资源的某个部分，即返回码是206（Partial Content），这样就方便了开发者自由的选择以便于充分利用带宽和连接。


3. 错误通知的管理，在HTTP1.1中新增了24个错误状态响应码，如409（Conflict）表示请求的资源与资源的当前状态发生冲突；410（Gone）表示服务器上的某个资源被永久性的删除。


4. Host头处理，在HTTP1.0中认为每台服务器都绑定一个唯一的IP地址，因此，请求消息中的URL并没有传递主机名（hostname）。但随着虚拟主机技术的发展，在一台物理服务器上可以存在多个虚拟主机（Multi-homed Web Servers），并且它们共享一个IP地址。HTTP1.1的请求消息和响应消息都应支持Host头域，且请求消息中如果没有Host头域会报告一个错误（400 Bad Request）。


5. 长连接，HTTP 1.1支持长连接（PersistentConnection）和请求的流水线（Pipelining）处理，在一个TCP连接上可以传送多个HTTP请求和响应，减少了建立和关闭连接的消耗和延迟，在HTTP1.1中默认开启Connection： keep-alive，一定程度上弥补了HTTP1.0每次请求都要创建连接的缺点。

### http2.0 与http1.0 的区别
https://www.cnblogs.com/heluan/p/8620312.html

1. 新的二进制格式，HTTP1.x 的解析是基于文本。基于文本协议的格式解析存在天然缺陷，文本的表现形式有多样性，要做到健壮性考虑的场景必然很多，二进制则不同，只认0和1 的组合。基于这种考虑HTTP2.0 的协议解析决定采用二进制格式，实现方便且健壮。

2. 多路复用，即连接共享，建立起一个连接请求后，可以在这个链接上一直发送，不要等待上一次发送完并且受到回复后才能发送下一个（http1.0 是这样），是可以同时发送多个请求，互相并不干扰。
3. header 压缩，header 带有大量信息，而且每次都要重复发送，HTTP2.0 利用HPACK 对消息头进行压缩传输，客服端和服务器维护一个动态链表（当一个头部没有出现的时候，就插入，已经出现了就用表中的索引值进行替代），将既避免了重复header 的传输，又减小了需要传输的大小。
4. 服务端推送（server push），就是客户端请求html 的时候，服务器顺带把此html 需要的css,js
也一起发送给客服端，而不像http1.0 中需要请求一次html，然后再请求一次css，然后再请求一
次js。

### 转发与重定向的区别
转发是服务器行为，重定向是客户端行为。

转发过程：客户浏览器发送http 请求->web 服务器接受此请求->调用内部的一个方法在容器内部完成请求处理和转发动作->将目标资源发送给客户；在这里，转发的路径必须是同一个web 容器下的url，其不能转向到其他的web 路径上去，中间传递的是自己的容器内的request。在客户浏览器路径栏显示的仍然是其第一次访问的路径，也就是说客户是感觉不到服务器做了转发的。转发行为是浏览器只做了一次访问请求。

重定向过程：客户浏览器发送http 请求->web 服务器接受后发送302 状态码响应及对应新的
location 给客户浏览器->客户浏览器发现是302 响应，则自动再发送一个新的http 请求，请求url 是新的location 地址->服务器根据此请求寻找资源并发送给客户。在这里location 可以重定向到任意URL，既然是浏览器重新发出了请求，则就没有什么request 传递的概念了。在客户浏览器路径栏显示的是其重定向的路径，客户可以观察到地址的变化的。重定向行为是浏览器做了至少两次的访问请求的。