package net.javapla.jawn.core;

import net.javapla.jawn.core.renderers.RendererEngine;

public class JawnMainTest extends Jawn {
    
    {
        use((app) -> {
            app.binder().bind(RendererEngine.class).toInstance(new RendererEngine() {

                @Override
                public void invoke(Context context, Object renderable) throws Exception {}

                @Override
                public MediaType[] getContentType() {
                    return new MediaType[] { MediaType.valueOf("kage/henning") };
                }
                
            });
        });
        
        get("/", Results.text("holaa"));
        get("/test", ctx -> Results.text("teeeest :: " + ctx.param("dd").orElse("")).status(Status.ALREADY_REPORTED));
        get("/xml", Results.xml("<xml>teeeest</xml>"));
        get("/json", Results.json("{\"key\":\"teeeest\"}"));
        
        post("/test/{dd}", ctx -> Results.text("teeeest :: " + ctx.param("dd").orElse("")).status(Status.ALREADY_REPORTED));
    }

    public static void main(String[] args) {
        run(JawnMainTest.class, args);
    }

}
