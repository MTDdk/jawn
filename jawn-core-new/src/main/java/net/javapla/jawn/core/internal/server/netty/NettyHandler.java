package net.javapla.jawn.core.internal.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AsciiString;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import net.javapla.jawn.core.server.ConnectionResetByPeer;
import net.javapla.jawn.core.server.HttpHandler;

class NettyHandler extends SimpleChannelInboundHandler<Object> {
    
    private static AsciiString STREAM_ID = HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text();
    public static final AttributeKey<String> PATH = AttributeKey.newInstance(NettyHandler.class.getName());
    

    private final HttpHandler handler;
    private final String tmpdir;
    private final int bufferSize;
    //private final int wsMaxMessageSize;

    NettyHandler(final HttpHandler dispatcher, final String tmpdir, final int bufferSize/*, final int wsBufferSize*/) {
        this.handler = dispatcher;
        this.tmpdir = tmpdir;
        this.bufferSize = bufferSize;
        //this.wsMaxMessageSize = wsBufferSize;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            ctx.channel().attr(NettyRequest.NEED_FLUSH).set(true);

            HttpRequest req = (HttpRequest) msg;
            ctx.channel().attr(PATH).set(req.method().name() + " " + req.uri());

            if (HttpUtil.is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
            }

            boolean keepAlive = HttpUtil.isKeepAlive(req);

            try {
                String streamId = req.headers().get(STREAM_ID);

                HttpHeaders headers = new DefaultHttpHeaders();
                handler.handle(
                    new NettyRequest(ctx, req, tmpdir),
                    new NettyResponse(ctx, headers, bufferSize, keepAlive, streamId));

            } catch (Throwable ex) {
                exceptionCaught(ctx, ex);
            }
        } /*else if (msg instanceof WebSocketFrame) {
            Attribute<NettyWebSocket> ws = ctx.channel().attr(NettyWebSocket.KEY);
            ws.get().handle(msg);
        }*/
    }
    
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        try {
            if (ConnectionResetByPeer.test(cause)) {
                //log.trace("execution of: " + ctx.channel().attr(PATH).get() + " resulted in error", cause);
            } else {
                /*Attribute<NettyWebSocket> ws = ctx.channel().attr(NettyWebSocket.KEY);
                if (ws != null && ws.get() != null) {
                    ws.get().handle(cause);
                } else */{
                    //log.debug("execution of: " + ctx.channel().attr(PATH).get() + " resulted in error",
                    //    cause);
                }
            }
        } finally {
            ctx.close();
        }

    }
    
    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        Attribute<Boolean> attr = ctx.channel().attr(NettyRequest.NEED_FLUSH);
        boolean needFlush = (attr == null || attr.get() == Boolean.TRUE);
        if (needFlush) {
            ctx.flush();
        }
    }
    
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt)
        throws Exception {
        // Idle timeout
        if (evt instanceof IdleStateEvent) {
            //log.debug("idle timeout: {}", ctx);
            ctx.close();
        } else if (evt instanceof HttpServerUpgradeHandler.UpgradeEvent) {
            // Write an HTTP/2 response to the upgrade request
            FullHttpRequest req = ((HttpServerUpgradeHandler.UpgradeEvent) evt).upgradeRequest();
            req.headers().set(STREAM_ID, req.headers().get(STREAM_ID, "1"));
            channelRead0(ctx, req);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
