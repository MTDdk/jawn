package implementation;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.Modes;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.WebSocket;
import net.javapla.jawn.core.filters.LogRequestTimingFilter;
import net.javapla.jawn.core.server.ServerConfig.Performance;

public class UndertowMainTest extends Jawn {
    
    {
        mode(Modes.DEV);
        server()
            .performance(Performance.MINIMUM)
            .port(8080);
        
        get("/t", Results.text("holaaaa5588")).before(SomeRandomClass::before);
        get("/xml", Results.xml("<xml>teeeest</xml>"));
        get("/json", Results.json("{\"key\":\"teeeest\"}"));
        
        get("/test", ctx -> Results.text("teeeest :: " + ctx.param("dd").value("")).status(201));
        post("/test/{dd}", ctx -> Results.text("teeeest :: " + ctx.param("dd").value("")).status(Status.ALREADY_REPORTED));
        get("/path/{.*}", ctx -> Results.text(ctx.req().path()));
        
        get("/", Results.view()/*.path("system")*//*.template("404").layout(null)*/);

        //mvc(TestController.class);
        controllers("implementation.controllers");
        
        filter(LogRequestTimingFilter.class);
        
        
        
        
        AtomicInteger counter = new AtomicInteger(0);
        Timer t = new Timer();
        
        ArrayList<WebSocket> wss = new ArrayList<>();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                int count = counter.incrementAndGet();
                wss.forEach(ws -> ws.send("something + " + count + "  "+ ws.hashCode()));
            } 
        }, 600, 1200);
        
        ws("/ws/{id}", (req, init) -> {
            int id = req.pathParam("id").asInt();
            
            init.onConnect(ws -> {
                System.out.println("connected ["+ id +"]   " + ws.hashCode());
                if (id == 7)
                    wss.add(ws);
                if (id == 73) {
                    t.schedule(new TimerTask() {
                        @Override
                        public void run() {ws.close();}
                    }, 8000);
                }
                    
            });
            init.onMessage((ws, message) -> System.out.println(message.value()));
            init.onClose((ws, status) -> { System.out.println("closed ["+id+"]   " + ws.hashCode()); wss.remove(ws); });
        });
        
    }

    public static void main(String[] args) {
        run(UndertowMainTest.class, args);
    }

}
