package cn.wy.netty.server.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.Future;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by 胡歌的小迷弟 on 2020/2/22 10:29
 */
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private URL baseURL = HttpHandler.class.getProtectionDomain().getCodeSource().getLocation();
    private final String WEB_ROOT="webroot";

    private File getFileFromRoot(String filename) throws Exception {
        String path=baseURL.toURI()+WEB_ROOT+"/"+filename;
        path=!path.startsWith("file:")?path:path.substring(5);
        path=path.replaceAll("//","/");
        return new File(path);

    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String uri = request.getUri();

        RandomAccessFile file = null;
        try{
            String page = uri.equals("/") ? "chat.html" : uri;
            file =	new RandomAccessFile(getFileFromRoot(page), "r");
        }catch(Exception e){
            ctx.fireChannelRead(request.retain());
            return;
        }

        HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);
        String contextType = "text/html;";
        if(uri.endsWith(".css")){
            contextType = "text/css;";
        }else if(uri.endsWith(".js")){
            contextType = "text/javascript;";
        }else if(uri.toLowerCase().matches("(jpg|png|gif)$")){
            String ext = uri.substring(uri.lastIndexOf("."));
            contextType = "image/" + ext;
        }
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, contextType + "charset=utf-8;");

        boolean keepAlive = HttpHeaders.isKeepAlive(request);

        if (keepAlive) {
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, file.length());
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
        ctx.write(response);

        ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
//        ctx.write(new ChunkedNioFile(file.getChannel()));

        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

        file.close();

    }
}
