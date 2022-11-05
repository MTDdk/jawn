package net.javapla.jawn.examples;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Up;

public class SimpleJawnMain extends Jawn {
    
    {
        
        get("/henning").after((ctx, result, error) -> {
            System.out.println(ctx.resp().status());
        });
        get("/test", ctx -> ctx.resp().contentType(MediaType.HTML).respond("<html><body><h1>Workzes</h1></body</html>".getBytes()));
        
        get("/stream", ctx -> {
            ctx.resp().contentType(MediaType.HTML);
            return new ByteArrayInputStream("TESTING LONG STREAM".getBytes());
        })
        .before(ctx -> System.out.println("streaming"))
        .before(ctx -> System.out.println("before streaming"))
        .postResponse((ctx,error) -> System.out.println("POST"));
        
        path("/api", () -> {
            get("/v1", ctx -> {
                System.out.println("api/v1  handler");
                return "---  api/v1";
            }).filter(new TimingFilter()).after((ctx,result,error) -> System.out.println("before1111"));
            get("/v2", ctx -> "api/v2");
        });
        
        post("/post", ctx -> {
            System.out.println(ctx.req().body().value(StandardCharsets.UTF_8));
        });
        
        get("/json", () -> {
            return new JsonResponse("key","value");
        }).produces(MediaType.JSON).filter(new TimingFilter());
    }
    
    public static record JsonResponse(String key, String value) {}
    
    private static class TimingFilter implements Route.Filter {

        @Override
        public void before(Context ctx) throws Up {
            ctx.attribute("timing", System.currentTimeMillis());
        }

        @Override
        public void after(Context ctx, Object result, Throwable cause) {}
        
        @Override
        public void onComplete(Context ctx, Throwable error) {
            System.out.println("Timing   ::   "  + (ctx.attribute("timing").map(time -> (System.currentTimeMillis() - ((long)time))).get()));
        }
        
    }

    public static void main(String[] args) {
        run();
    }
}
