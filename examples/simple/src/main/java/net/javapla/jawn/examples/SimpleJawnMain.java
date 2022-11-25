package net.javapla.jawn.examples;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.Up;

public class SimpleJawnMain extends Jawn {
    
    private static final String MESSAGE = "Hello, World!";
    private static final ByteBuffer MESSAGE_BUFFER = ByteBuffer
        .allocateDirect(MESSAGE.length())
        .put(MESSAGE.getBytes(StandardCharsets.US_ASCII))
        .flip();
    
    {
        
        get("/henning", Status.ACCEPTED).after((ctx, result, error) -> {
            // TODO document why these are different in this specific case
            System.out.println(ctx.resp().status()); // the result has not yet been added to the context at this point
            System.out.println(result); // "after" is called directly after "handler", so context will not be finalised yet
        });
        get("/html", ctx -> ctx.resp().respond("<html><body><h1>Workzes</h1></body</html>".getBytes())).produces(MediaType.HTML);
        
        get("/redirect", ctx -> ctx.resp().redirectSeeOther("/henning"));
        
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
            System.out.println(ctx.req().contentType());
            JsonResponse parse = ctx.req().parse(JsonResponse.class);
            System.out.println(parse);
            return Status.ACCEPTED;
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
