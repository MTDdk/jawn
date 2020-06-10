package net.javapla.jawn.server.undertow;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.WebSocket;
import net.javapla.jawn.core.WebSocketCloseStatus;
import net.javapla.jawn.core.WebSocketMessage;
import net.javapla.jawn.core.server.Server;

class UndertowWebSocket extends AbstractReceiveListener implements WebSocket.Listener, WebSocket, WebSocketCallback<Void> {
    
    //private static final ConcurrentMap<String, List<WebSocket>> ALL = new ConcurrentHashMap<>();
    
    private final Config config;
    private final UndertowRequest req;
    private final WebSocketChannel channel;
    private final boolean dispatch;
    //private final String key;
    private final CountDownLatch ready = new CountDownLatch(1);

    private WebSocket.OnConnect onConnectCallback;
    private WebSocket.OnMessage onMessageCallback;
    private WebSocket.OnError onErrorCallback;
    private WebSocket.OnClose onCloseCallback;
    
    

    public UndertowWebSocket(Config config, UndertowRequest req, WebSocketChannel channel) {
        this.config = config;
        this.req = req;
        this.channel = channel;
        this.dispatch = req.isInIoThread();
        //this.key = req.path(); // ctx.getRoute().getPattern();
    }

    /* WebSocket.Configurer */
    @Override
    public WebSocket.Listener onConnect(WebSocket.OnConnect callback) {
        this.onConnectCallback = callback;
        return this;
    }

    @Override
    public WebSocket.Listener onMessage(WebSocket.OnMessage callback) {
        this.onMessageCallback = callback;
        return this;
    }

    @Override
    public WebSocket.Listener onError(WebSocket.OnError callback) {
        this.onErrorCallback = callback;
        return this;
    }

    @Override
    public WebSocket.Listener onClose(WebSocket.OnClose callback) {
        this.onCloseCallback = callback;
        return this;
    }

    /* WebSocket */
    @Override
    public WebSocket send(String message/*, boolean broadcast*/) {
        if (isOpen()) {
            try {
                WebSockets.sendText(message, channel, this);
            } catch (Throwable e) {
                onError(channel, e);
            }
        } else {
            onError(channel, new IllegalStateException("Attemp to send a message on a closed web socket"));
        }
        
        return this;
    }

    @Override
    public WebSocket send(byte[] message/*, boolean broadcast*/) {
        /*if (broadcast) {
            List<WebSocket> list = ALL.get(key);
            if (list != null) {
                list.forEach(ws -> ws.send(message, false));
            }
        } else {*/
            if (isOpen()) {
                try {
                    WebSockets.sendBinary(ByteBuffer.wrap(message), channel, this);
                } catch (Throwable e) {
                    onError(channel, e);
                }
            } else {
                onError(channel, new IllegalStateException("Attemp to send a message on a closed web socket"));
            }
        //}
        
        return this;
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();// && !channel.isCloseFrameSent();
    }

    /*@Override
    public List<WebSocket> sessions() {
        List<WebSocket> list = ALL.get(key);
        if (list == null) return Collections.emptyList();
        ArrayList<WebSocket> result = new ArrayList<>(list);
        result.remove(this);
        return result;
    }*/

    /*@Override
    public WebSocket render(Object value, boolean broadcast) {
        return null;
    }*/

    @Override
    public WebSocket close(WebSocketCloseStatus status) {
        //System.out.println("close(WebSocketCloseStatus status)");
        handleClose(status);
        return this;
    }

    /* WebSocketCallback */
    @Override
    public void complete(WebSocketChannel channel, Void context) {
        // NOOP
    }

    @Override
    public void onError(WebSocketChannel channel, Void context, Throwable throwable) {
        onError(channel, throwable);
    }

    
    /* AbstractReceiveListener */
    @Override
    protected long getMaxTextBufferSize() {
        return config.getMemorySize("server.ws.max_text_message_size");//WebSocket.MAX_BUFFER_SIZE;
    }
    
    @Override
    protected long getMaxBinaryBufferSize() {
        return config.getMemorySize("server.ws.max_binary_missage_size");//WebSocket.MAX_BUFFER_SIZE;
    }
    
    /*@Override
    protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
        super.onFullBinaryMessage(channel, message);
    }*/
    
    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
        waitForConnect();
        
        if (onMessageCallback != null) {
            dispatch(webSocketTask(() -> onMessageCallback.onMessage(this, WebSocketMessage.create(message.getData())), false));
        }
    }
    
    @Override
    protected void onError(WebSocketChannel channel, Throwable throwable) {
        //
        if (Server.connectionResetByPeer(throwable) || Up.isFatal(throwable)) {
            //System.out.println("onError(WebSocketChannel channel, Throwable throwable)");
            handleClose(WebSocketCloseStatus.SERVER_ERROR);
        }
        
        if (onErrorCallback == null) {
            // TODO log this as an error
            System.err.println("Websocket exception: " + req.path() + " -> " + throwable);
        } else {
            onErrorCallback.onError(this, throwable);
        }
        
        if (Up.isFatal(throwable)) throw Up.IO.because(throwable);
    }
    
    @Override
    protected void onCloseMessage(CloseMessage cm, WebSocketChannel channel) {
        //System.out.println("onCloseMessage(CloseMessage cm, WebSocketChannel channel)");
        //if (channel.isCloseFrameSent()) return;
        
        handleClose(
            WebSocketCloseStatus
                .valueOf(cm.getCode())
                .orElseGet(() -> new WebSocketCloseStatus(cm.getCode(), cm.getReason())));
    }
    
    private void handleClose(WebSocketCloseStatus status) {
        
        if (!channel.isCloseFrameSent())
            WebSockets.sendClose(status.code(), status.reason(), channel, this);
        
        try {
            
            if (onCloseCallback != null) {
                onCloseCallback.onClose(this, status);
            }
            
        } catch (Throwable e) {
            onError(channel, e);
        } /*finally {
            removeSession(this);
          }*/
        
    }
    
    /*private void addSession(UndertowWebSocket ws) {
        ALL.computeIfAbsent(ws.key, k -> new CopyOnWriteArrayList<>()).add(ws);
    }
    
    private void removeSession(UndertowWebSocket ws) {
        List<WebSocket> list = ALL.get(ws.key);
        if (list != null) {
            list.remove(ws);
        }
    }*/
    
    void fireConnect() {
        // fire only once
        try {
            //addSession(this);
            // read from config
            long timeout = config.getDuration("server.ws.idle_timeout", TimeUnit.MILLISECONDS);
            if (timeout > 0) {
                channel.setIdleTimeout(timeout);
            }
            if (onConnectCallback != null) {
                dispatch(webSocketTask(() -> onConnectCallback.onConnect(this), true));
            } else {
                ready.countDown();
            }
            channel.getReceiveSetter().set(this);
            channel.resumeReceives();
        } catch (Throwable e) {
            onError(channel, e);
        }
    }
    
    private Runnable webSocketTask(Runnable task, boolean isInit) {
        return () -> {
            try {
                task.run();
            } catch (Throwable e) {
                onError(null, e);
            } finally {
                if (isInit) ready.countDown();
            }
        };
    }
    
    private void dispatch(Runnable task) {
        if (dispatch) {
            req.worker().execute(task);
        } else {
            task.run();
        }
    }
    
    private void waitForConnect() {
        try {
            ready.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /*@Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }*/
}
