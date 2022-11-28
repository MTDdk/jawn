package net.javapla.jawn.examples;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.WebSocket;
import net.javapla.jawn.core.util.StringUtil;

public class WebSocketJawnMain extends Jawn {
    
    static final ConcurrentHashMap<String, Map<WebSocket, WSHolder>> CONNECTIONS = new ConcurrentHashMap<>();
    
    static int count = 0;
    {
        // ?type=occurrence
        // ?type=fullstate,occurrence
        // ?type=occurrence[kills,round_start]
        // ?type=occurrence[round_ended:winCondition[bomb_exploded]]
        // ?snapshot=620 (from this)
        ws("/ws", (req, init) -> {
            System.out.println(req.path());
            Map<String, String> query = query(req.queryString());
            System.out.println(query);
            System.out.println(query.get("henning"));
            
            init.onConnect(ws -> {
                System.out.println(ws);
                
                Map<WebSocket, WSHolder> sockets = CONNECTIONS.computeIfAbsent(req.path(), path -> new ConcurrentHashMap<>());//CopyOnWriteArrayList<>());
                Timer c = new Timer(true);
                c.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ws.send("connected");
                    }
                }, TimeUnit.SECONDS.toMillis(2));
                
                Timer p = new Timer(true);
                p.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ws.ping();
                        //if (count++ == 20) ws.close();
                    }
                }, TimeUnit.SECONDS.toMillis(1));//, TimeUnit.SECONDS.toMillis(5));
                
                
                sockets.put(ws, new WSHolder(ws, () -> {
                    c.cancel();
                    p.cancel();
                }));
                
                
            });
            
            init.onMessage((ws,msg) -> {
                System.out.println(msg.value());
                ws.send(msg.value() + "  " + count++);
                if (count == 5) ws.close();
            });
            
            init.onClose((ws, status) -> {
                Map<WebSocket, WSHolder> sockets = CONNECTIONS.get(req.path());
                if (sockets != null) {
                    WSHolder holder = sockets.remove(ws);
                    if (holder != null)
                        holder.cleanup.run();
                }
                System.out.println("closed1 " + status + " " + sockets.size());
            });
        });
        
        ws("/wsanother", (req, init) -> {
            init.onConnect(ws -> System.out.println(ws));
            init.onMessage((ws,msg) -> System.out.println(msg.value()));
            init.onClose((ws, status) -> System.out.println("closed2 "));
        });
    }
    
    public static void main(String[] args) {
        run();
    }

    static record WSHolder (WebSocket ws, Runnable cleanup) {
        public int hashCode() {
            return ws.hashCode();
        }
        public boolean equals(WebSocket w) {
            return ws.equals(w);
        }
    }
    
    static Map<String, String> query(String query) {
        LinkedHashMap<String,String> map = new LinkedHashMap<>(2);
        if (query.length() > 1) {
            StringUtil.split(StringUtil.split(query, '&'), '=', map::put);
        }
        return map;
    }
}
