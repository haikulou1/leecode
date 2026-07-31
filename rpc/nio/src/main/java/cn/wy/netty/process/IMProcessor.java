package cn.wy.netty.process;

import cn.wy.netty.protocol.IMDecoder;
import cn.wy.netty.protocol.IMEncoder;
import cn.wy.netty.protocol.IMMessage;
import cn.wy.netty.protocol.IMP;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Date;

/**
 * Created by 胡歌的小迷弟 on 2020/2/23 10:23
 */
public class IMProcessor {

    private final static ChannelGroup onlineUsers=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private IMDecoder decoder=new IMDecoder();
    private IMEncoder encoder=new IMEncoder();

    private final AttributeKey<String> NICK_NAME=AttributeKey.valueOf("nickName");

    private final AttributeKey<String> IP_ADDR=AttributeKey.valueOf("ipAddr");
    private final AttributeKey<String> ATTRS=AttributeKey.valueOf("attrs");

    public void logout(Channel client){

        onlineUsers.remove(client);
    }



//    public void process(Channel client,)

    public void process(Channel client,String msg){
        IMMessage request=decoder.decode(msg);
        String nickname=request.getSender();
        if(null==request){
            return;
        }




        if(IMP.LOGIN.getName().equals(request.getCmd())){
            client.attr(NICK_NAME).getAndSet(request.getSender());
            client.attr(IP_ADDR).getAndSet(request.getAddr());
            onlineUsers.add(client);

            for(Channel channle:onlineUsers){
                if(client!=channle){

                    request=new IMMessage(IMP.SYSTEM.getName(),sysTime(),onlineUsers.size(),nickname+"加入聊天");
                }else{
                    request=new IMMessage(IMP.SYSTEM.getName(),sysTime(),onlineUsers.size(),nickname+"已在线");
                }
                String text=encoder.encode(request);
                channle.writeAndFlush(text);
                System.out.println(text);
            }
        }else if(IMP.LOGOUT.getName().equals(request.getCmd())){
            onlineUsers.remove(client);
        }else if(IMP.CHAT.getName().equals(request.getCmd())){
            for(Channel channel:onlineUsers){
                if(channel!=client){
                   // request=new IMMessage(IMP.SYSTEM.getName(),sysTime(),onlineUsers.size(),nickname);
                    request.setSender(client.attr(NICK_NAME).get());
                }else {
                    request.setSender("you");
                }
                String text=encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(text));
            }
        }
    }

    public long sysTime(){
        return System.currentTimeMillis();
    }

}
