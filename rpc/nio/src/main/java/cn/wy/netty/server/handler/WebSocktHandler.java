package cn.wy.netty.server.handler;

import cn.wy.netty.process.IMProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * Created by 胡歌的小迷弟 on 2020/2/23 9:37
 */
public class WebSocktHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private IMProcessor processor=new IMProcessor();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        processor.process(ctx.channel(),msg.text());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        processor.logout(ctx.channel());
    }
}
