package net.javapla.jawn.examples;

import net.javapla.jawn.core.Jawn;

public class WebSocketJawnMain extends Jawn {
    
    {
        ws("/ws", (req, init) -> {
            init.onConnect(ws -> System.out.println(ws));
            init.onMessage((ws,msg) -> System.out.println(ws));
        });
    }
    
    public static void main(String[] args) {
        run();
    }

}
