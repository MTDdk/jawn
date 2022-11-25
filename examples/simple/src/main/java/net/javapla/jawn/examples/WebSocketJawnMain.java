package net.javapla.jawn.examples;

import net.javapla.jawn.core.Jawn;

public class WebSocketJawnMain extends Jawn {
    
    {
        ws("/ws", (req, init) -> {
            init.onConnect(ws -> System.out.println(ws));
            init.onMessage((ws,msg) -> System.out.println(msg.value()));
            init.onClose((ws, status) -> System.out.println("closed1 " + status));
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

}
