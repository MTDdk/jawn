package net.javapla.jawn.core.internal.server.netty;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.server.HttpHandler;
import net.javapla.jawn.core.server.Server;

@Singleton
class NettyServer implements Server {
    
    private final HttpHandler dispatcher;
    private final Config conf;
    
    private EventLoopGroup bossLoop;

    private EventLoopGroup workerLoop;
    
    private Channel ch;
    
    private DefaultEventExecutorGroup executor;

    
    @Inject
    NettyServer(final HttpHandler dispatcher, final Config conf) {
        this.dispatcher = dispatcher;
        this.conf = conf;
    }

    @Override
    public void start() throws Exception {
        int bossThreads = 1;//conf.getInt("netty.threads.Boss");
        bossLoop = eventLoop(bossThreads, "boss");
        int workerThreads = Runtime.getRuntime().availableProcessors() * 2;//conf.getInt("netty.threads.Worker");
        if (workerThreads > 0) {
            workerLoop = eventLoop(Math.max(4, workerThreads), "worker");
        } else {
            workerLoop = bossLoop;
        }

        ThreadFactory threadFactory = new DefaultThreadFactory("jawn server netty task"/*conf.getString("netty.threads.Name")*/);
        this.executor = new DefaultEventExecutorGroup(workerThreads*4/*conf.getInt("netty.threads.Max")*/, threadFactory);

        this.ch = bootstrap(executor,/* null,*/ 8080/*conf.getInt("application.port")*/);

        /*boolean securePort = false;//conf.hasPath("application.securePort");

        if (securePort) {
            bootstrap(executor, NettySslContext.build(conf), conf.getInt("application.securePort"));
        }*/
    }

    @Override
    public void stop() throws Exception {
        shutdownGracefully(List.of(bossLoop, workerLoop, executor).iterator());
    }

    @Override
    public void join() throws InterruptedException {
        ch.closeFuture().sync();
    }
    
    @Override
    public Optional<Executor> executor() {
        return Optional.ofNullable(executor);
    }
    
    private Channel bootstrap(final EventExecutorGroup executor, /*final SslContext sslCtx,*/
                              final int port) throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();

        boolean epoll = bossLoop instanceof EpollEventLoopGroup;
        bootstrap.group(bossLoop, workerLoop)
        .channel(epoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
        .handler(new LoggingHandler(Server.class, LogLevel.DEBUG))
        .childHandler(new NettyPipeline(executor, dispatcher, conf/*, sslCtx*/));

        /*configure(conf.getConfig("netty.options"), "netty.options",
            (option, value) -> bootstrap.option(option, value));

        configure(conf.getConfig("netty.worker.options"), "netty.worker.options",
            (option, value) -> bootstrap.childOption(option, value));*/
        
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);

        return bootstrap
            .bind("0.0.0.0"/*host(conf.getString("application.host"))*/, port)
            .sync()
            .channel();
    }
    
    private EventLoopGroup eventLoop(final int threads, final String name) {
        //log.debug("netty.threads.{}({})", name, threads);
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup(threads, new DefaultThreadFactory("epoll-" + name, false));
        }
        return new NioEventLoopGroup(threads, new DefaultThreadFactory("nio-" + name, false));
    }
    
    
    /**
     * Shutdown executor in order.
     *
     * @param iterator Executors to shutdown.
     */
    private void shutdownGracefully(final Iterator<EventExecutorGroup> iterator) {
        if (iterator.hasNext()) {
            EventExecutorGroup group = iterator.next();
            if (!group.isShuttingDown()) {
                group.shutdownGracefully().addListener(future -> {
                    if (!future.isSuccess()) {
                        //log.debug("shutdown of {} resulted in exception", group, future.cause());
                    }
                    shutdownGracefully(iterator);
                });
            }
        }
    }

}
