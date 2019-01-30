package net.javapla.jawn.core.internal.server.netty;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import net.javapla.jawn.core.server.HttpHandler;
import net.javapla.jawn.core.server.Server;
import net.javapla.jawn.core.server.ServerConfig;

@Singleton
class NettyServer implements Server {
    
    private final HttpHandler dispatcher;
    
    private EventLoopGroup bossLoop;
    private EventLoopGroup workerLoop;
    private Channel ch;
    private DefaultEventExecutorGroup executor;


    
    @Inject
    NettyServer(final HttpHandler dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void start(final ServerConfig.Impl serverConfig) throws Exception {

        this.ch = bootstrap(executor, serverConfig/* null*/);

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
    
    private Channel bootstrap(final EventExecutorGroup executor, ServerConfig.Impl serverConfig /*final SslContext sslCtx*/) throws InterruptedException {
        ServerBootstrap builder = new ServerBootstrap();
        
        configureServerPerformance(builder, serverConfig);

        boolean epoll = bossLoop instanceof EpollEventLoopGroup;
        builder.group(bossLoop, workerLoop)
            .channel(epoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
            .handler(new LoggingHandler(Server.class, LogLevel.DEBUG))
            .childHandler(new NettyPipeline(executor, dispatcher/*, sslCtx*/));

        /*configure(conf.getConfig("netty.options"), "netty.options",
            (option, value) -> bootstrap.option(option, value));

        configure(conf.getConfig("netty.worker.options"), "netty.worker.options",
            (option, value) -> bootstrap.childOption(option, value));*/
        

        return builder
            .bind(serverConfig.host(), serverConfig.port())
            .sync()
            .channel();
    }
    
    private EventLoopGroup eventLoop(ServerBootstrap bootstrap, final int threads, final String name) {
        //log.debug("netty.threads.{}({})", name, threads);
        if (Epoll.isAvailable()) {
            bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
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
                        System.err.println("shutdown of " + group + " resulted in exception");
                        future.cause().printStackTrace();
                    }
                    shutdownGracefully(iterator);
                });
            }
        }
    }
    
    private void configureServerPerformance(ServerBootstrap builder, ServerConfig.Impl serverConfig) {
        
        int minimum = 1;//at least one thread for the bossLoop
        int ioThreads, workerThreads,executorThreads;
        switch (serverConfig.performance()) {
            case HIGHEST:
                ioThreads = Math.max(Runtime.getRuntime().availableProcessors() * 2, minimum);
                workerThreads = ioThreads * 4;
                executorThreads = Math.max(32, workerThreads * 4);
                break;
            default:
            case MINIMUM:
                ioThreads = minimum;
                workerThreads = ioThreads;
                executorThreads = workerThreads * 4;
                break;
            case CUSTOM:
                ioThreads = Math.max(serverConfig.ioThreads(), minimum);
                workerThreads = ioThreads * 4;
                executorThreads = workerThreads * 4;
                break;
        }
        
        this.bossLoop = eventLoop(builder, ioThreads, "boss");
        this.workerLoop = eventLoop(builder, Math.max(4, workerThreads), "worker");
        this.executor = new DefaultEventExecutorGroup(executorThreads, new DefaultThreadFactory("jawn-server-netty-task"));
        
        builder.option(ChannelOption.SO_BACKLOG, serverConfig.backlog());
        builder.option(ChannelOption.SO_REUSEADDR, true);
        builder.childOption(ChannelOption.SO_REUSEADDR, true);
    }

}
