package net.javapla.jawn.examples;

import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.MediaType;

public class SimpleJawnMain extends Jawn {
    
    {
        System.out.println("simply");
        
        get("/henning").after((ctx, result, error) -> {
            
        System.out.println(ctx.resp().status());
        });
        get("/test", ctx -> ctx.resp().contentType(MediaType.HTML).respond("<html><body><h1>Workzes</h1></body</html>".getBytes()));
    }

    public static void main(String[] args) {
        run();
    }

}
