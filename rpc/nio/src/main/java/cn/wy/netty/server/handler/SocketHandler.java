package cn.wy.netty.server.handler;

import cn.wy.netty.protocol.IMMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Created by 胡歌的小迷弟 on 2020/2/27 9:55
 */
public class SocketHandler extends SimpleChannelInboundHandler<IMMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMMessage msg) throws Exception {

    }
}
