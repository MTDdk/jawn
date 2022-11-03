package net.javapla.jawn.examples;

import java.io.ByteArrayInputStream;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Up;

public class SimpleJawnMain extends Jawn {
    
    {
        System.out.println("simply");
        
        get("/henning").after((ctx, result, error) -> {
            System.out.println(ctx.resp().status());
        });
        get("/test", ctx -> ctx.resp().contentType(MediaType.HTML).respond("<html><body><h1>Workzes</h1></body</html>".getBytes()));
        
        get("/stream", ctx -> {
            return new ByteArrayInputStream("TESTING LONG STREAM".getBytes());
        }).before(ctx -> System.out.println("streaming")).before(ctx -> System.out.println("before streaming")).postResponse((ctx,error) -> System.out.println("POST"));
        
        path("/api", () -> {
            get("/v1", ctx -> {
                System.out.println("api/v1  handler");
                return "---  api/v1";
            }).produces(MediaType.JSON).filter(new Route.Filter() {
                @Override
                public void before(Context ctx) throws Up {ctx.attribute("timing", System.currentTimeMillis()); System.out.println(ctx.req().path() +  " before");}

                @Override
                public void after(Context ctx, Object result, Throwable cause) {System.out.println(ctx.req().path() + " after");}
                
                public void onComplete(Context ctx, Throwable error) {
                    System.out.println("Timing   ::   "  + (ctx.attribute("timing").map(time -> (System.currentTimeMillis() - ((long)time))).get()));
                }
                
            }).after((ctx,result,error) -> System.out.println("before1111"));
            get("/v2", ctx -> "api/v2");
        });
    }

    public static void main(String[] args) {
        run();
    }

}
