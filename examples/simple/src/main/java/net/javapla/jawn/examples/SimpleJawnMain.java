package net.javapla.jawn.examples;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Up;

public class SimpleJawnMain extends Jawn {
    
    private static final ByteBuffer MESSAGE_BUFFER = ByteBuffer
        .allocateDirect(13)
        .put("Hello, World!".getBytes(StandardCharsets.US_ASCII))
        .flip();
    
    {
        
        get("/henning").after((ctx, result, error) -> {
            System.out.println(ctx.resp().status());
        });
        get("/html", ctx -> ctx.resp().respond("<html><body><h1>Workzes</h1></body</html>".getBytes())).produces(MediaType.HTML);
        
        get("/stream", ctx -> {
            ctx.resp().contentType(MediaType.HTML);
            return new ByteArrayInputStream("TESTING LONG STREAM".getBytes());
        })
        .before(ctx -> System.out.println("streaming"))
        .before(ctx -> System.out.println("before streaming"))
        .postResponse(ctx -> System.out.println("POST"));
        
        path("/api", () -> {
            get("/v1", ctx -> {
                System.out.println("api/v1  handler");
                return "---  api/v1";
            }).filter(new TimingFilter()).after((ctx,result,error) -> System.out.println("before1111"));
            get("/v2", ctx -> "api/v2");
        });
        
        get("/plaintext", ctx -> MESSAGE_BUFFER.duplicate());//ctx -> ctx.resp().respond(MESSAGE_BUFFER.duplicate()));
        
        post("/post", ctx -> {
            System.out.println(ctx.req().body().value(StandardCharsets.UTF_8));
        });
        
        get("/json", () -> {
            return new JsonResponse("key","value");
        }).produces(MediaType.JSON).filter(new TimingFilter());
    }
    
    public static record JsonResponse(String key, String value) {}
    
    private static class TimingFilter extends Route.Filter {
        @Override
        public void before(Context ctx) throws Up {
            ctx.attribute("timing", System.currentTimeMillis());
        }
        
        @Override
        public void complete(Context ctx) {
            System.out.println("Timing   ::   "  + (ctx.attribute("timing").map(time -> (System.currentTimeMillis() - ((long)time))).get()));
        }
    }

    public static void main(String[] args) {
        run();
    }
}
