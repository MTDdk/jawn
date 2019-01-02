package net.javapla.jawn.core.internal.server.netty;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.server.HttpHandler;

final class NettyPipeline extends ChannelInitializer<SocketChannel> {
    
    // TODO should be instantiated in the core module instead of a server module
    private static final Path TMP_DIR = Paths.get(System.getProperty("java.io.tmpdir")+"/jawn" /*+application name*/);
    static {
        if (!TMP_DIR.toFile().exists()) TMP_DIR.toFile().mkdirs();
    }
    
    private EventExecutorGroup executor;

    private HttpHandler handler;

    private Config config;

    private int maxInitialLineLength;

    private int maxHeaderSize;

    private int maxChunkSize;

    int maxContentLength;

    private long idleTimeOut;

    //private SslContext sslCtx;

    private boolean supportH2;

    //private String tmpdir;

    private int bufferSize;

    private int wsMaxMessageSize;
    
    NettyPipeline(final EventExecutorGroup executor, final HttpHandler dispatcher, final Config conf) {
        this.executor = executor;
        this.handler = dispatcher;
        this.config = conf;
        
        maxInitialLineLength = 4_000;
        maxHeaderSize = 8_000;
        maxChunkSize = 16_000;
        maxContentLength = 200_000;
        idleTimeOut = 0;
        supportH2 = false;
        this.wsMaxMessageSize = 16_000; /*Math
            .max(
                config.getBytes("server.ws.MaxTextMessageSize").intValue(),
                config.getBytes("server.ws.MaxBinaryMessageSize").intValue());*/

    }


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        final ChannelPipeline p = ch.pipeline();
        /*if (sslCtx != null) {
            p.addLast("ssl", sslCtx.newHandler(ch.alloc()));
            p.addLast("h1.1/h2", new Http2OrHttpHandler());
        } else {
            if (supportH2) {
                p.addLast("h2c", new Http2PrefaceOrHttpHandler());

                idle(p);

                aggregator(p);

                jooby(p);
            } else */{
                http1(p);
            }
        //}
    }

    private void http1(final ChannelPipeline p) {
        p.addLast("codec", http1Codec());

        idle(p);

        aggregator(p);

        framework(p);
    }
    
    private HttpServerCodec http1Codec() {
        return new HttpServerCodec(maxInitialLineLength, maxHeaderSize, maxChunkSize, false);
    }

    private void idle(final ChannelPipeline p) {
        if (idleTimeOut > 0) {
            p.addLast("timeout", new IdleStateHandler(0, 0, idleTimeOut, TimeUnit.MILLISECONDS));
        }
    }
    
    private void aggregator(final ChannelPipeline p) {
        p.addLast("aggregator", new HttpObjectAggregator(maxContentLength));
    }

    private void framework(final ChannelPipeline p) {
        p.addLast(executor, "framework", new NettyHandler(handler, TMP_DIR.toString(), bufferSize/*, wsMaxMessageSize*/));
    }

}
