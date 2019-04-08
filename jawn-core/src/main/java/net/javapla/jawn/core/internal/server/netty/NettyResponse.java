package net.javapla.jawn.core.internal.server.netty;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.io.ByteStreams;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.Attribute;
import net.javapla.jawn.core.server.ServerResponse;

public class NettyResponse implements ServerResponse {

    
    private /*final*/ ChannelHandlerContext ctx;
    private final int bufferSize;
    private final boolean keepAlive;
    private final HttpHeaders headers;
    private HttpResponseStatus status;
    
    private boolean committed;


    NettyResponse(final ChannelHandlerContext ctx, final HttpHeaders headers, final int bufferSize, final boolean keepAlive) {
        this(ctx, headers, bufferSize, keepAlive, null);
    }
    
    NettyResponse(final ChannelHandlerContext ctx, final HttpHeaders headers, final int bufferSize, final boolean keepAlive, final String streamId) {
        this.ctx = ctx;
        this.bufferSize = bufferSize;
        this.keepAlive = keepAlive;
        this.headers = headers;
        if (streamId != null) {
            headers.set(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), streamId);
        }
        this.status = HttpResponseStatus.OK;
    }

    @Override
    public Optional<String> header(final String name) {
        return Optional.ofNullable(this.headers.get(name));
    }

    @Override
    public List<String> headers(final String name) {
        List<String> headers = this.headers.getAll(name);
        return headers == null ? Collections.emptyList() : Collections.unmodifiableList(headers);
    }

    @Override
    public void header(final String name, final List<String> values) {
        headers.remove(name).add(name, values);
    }

    @Override
    public void header(final String name, final String value) {
        headers.set(name, value);
    }

    @Override
    public void removeHeader(final String name) {
        headers.remove(name);
    }

    @Override
    public void send(final byte[] bytes) throws Exception {
        _send(Unpooled.wrappedBuffer(bytes));
    }

    @Override
    public void send(final ByteBuffer buffer) throws Exception {
        _send(Unpooled.wrappedBuffer(buffer));
    }

    @Override
    public void send(final InputStream stream) throws Exception {
        byte[] chunk = new byte[bufferSize];
        int count = ByteStreams.read(stream, chunk, 0, bufferSize);//TODO use something else than Googles ByteStreams
        if (count <= 0) {
          return;
        }
        ByteBuf buffer = Unpooled.wrappedBuffer(chunk, 0, count);
        if (count < bufferSize) {
          _send(buffer);
        } else {
          DefaultHttpResponse rsp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);

          boolean lenSet = headers.contains(HttpHeaderNames.CONTENT_LENGTH);
          final boolean keepAlive;
          final ChannelPromise promise;
          if (!lenSet) {
            headers.set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            keepAlive = false;
            promise = ctx.newPromise();
          } else if (this.keepAlive) {
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            keepAlive = this.keepAlive;
            promise = ctx.voidPromise();
          } else {
            keepAlive = false;
            promise = ctx.newPromise();
          }

          // dump headers
          rsp.headers().set(headers);
          ChannelHandlerContext ctx = this.ctx;
          ctx.channel().attr(NettyRequest.NEED_FLUSH).set(false);

          // add chunker
          chunkHandler(ctx.pipeline());

          // group all write
          ctx.channel().eventLoop().execute(() -> {
            // send headers
            ctx.write(rsp);
            // send head chunk
            ctx.write(buffer);
            // send tail
            ctx.write(new ChunkedStream(stream, bufferSize));
            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT, promise);
            if (!keepAlive) {
              future.addListener(ChannelFutureListener.CLOSE);
            }
          });
        }

        committed = true;
    }

    @Override
    public void send(final FileChannel channel) throws Exception {
        //TODO
    }

    @Override
    public int statusCode() {
        return status.code();
    }

    @Override
    public void statusCode(final int code) {
        this.status = HttpResponseStatus.valueOf(code);
    }

    @Override
    public boolean committed() {
        return committed;
    }

    @Override
    public void end() {
        if (ctx != null) {
            /*Attribute<NettyWebSocket> ws = ctx.channel().attr(NettyWebSocket.KEY);
            if (ws != null && ws.get() != null) {
                status = HttpResponseStatus.SWITCHING_PROTOCOLS;
                ws.get().hankshake();
                ctx = null;
                committed = true;
                return;
            }*/
            if (!committed) {
                DefaultHttpResponse rsp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
                headers.set(HttpHeaderNames.CONTENT_LENGTH, 0);
                // dump headers
                rsp.headers().set(headers);
                if (keepAlive) {
                    ctx.write(rsp, ctx.voidPromise());
                } else {
                    ctx.write(rsp).addListener(ChannelFutureListener.CLOSE);
                }
                ctx.flush();
                committed = true;
            }
            ctx = null;
        }
    }

    @Override
    public void reset() {
        headers.clear();
        status = HttpResponseStatus.OK;
    }

    @Override
    public Writer writer() {
        return null;//TODO remove?
    }

    @Override
    public OutputStream outputStream() {
        return null;//TODO remove?
    }

    @Override
    public boolean usingStream() {
        return false;//TODO
    }

    private void _send(final ByteBuf buffer) throws Exception {
        DefaultFullHttpResponse rsp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buffer);

        headers.remove(HttpHeaderNames.TRANSFER_ENCODING)
        .set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());

        ChannelPromise promise;
        if (keepAlive) {
            promise = ctx.voidPromise();
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        } else {
            promise = ctx.newPromise();
        }

        // dump headers
        rsp.headers().set(headers);

        Attribute<Boolean> async = ctx.channel().attr(NettyRequest.ASYNC);
        boolean flush = async != null && async.get() == Boolean.TRUE;
        final ChannelFuture future;
        if (flush) {
            future = ctx.writeAndFlush(rsp, promise);
        } else {
            future = ctx.write(rsp, promise);
        }
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

        committed = true;
    }
    
    private void chunkHandler(final ChannelPipeline pipeline) {
        if (pipeline.get("chunker") == null) {
            pipeline.addAfter("codec", "chunker", new ChunkedWriteHandler());
        }
    }

}
