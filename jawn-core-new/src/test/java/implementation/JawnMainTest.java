package implementation;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.filters.LogRequestTimingFilter;
import net.javapla.jawn.core.renderers.RendererEngine;

public class JawnMainTest extends Jawn {
    
    {
        use((app) -> {
            app.binder().bind(RendererEngine.class).toInstance(new RendererEngine() {

                @Override
                public void invoke(Context context, Object renderable) throws Exception {
                    
                }

                @Override
                public MediaType[] getContentType() {
                    return new MediaType[] { MediaType.valueOf("kage/henning") };
                }
                
            });
        });
        
        get("/", Results.text("holaaaa55"));
        get("/test", ctx -> Results.text("teeeest :: " + ctx.param("dd").orElse("")).status(Status.ALREADY_REPORTED));
        get("/xml", Results.xml("<xml>teeeest</xml>"));
        get("/json", Results.json("{\"key\":\"teeeest\"}"));
        
        post("/test/{dd}", ctx -> Results.text("teeeest :: " + ctx.param("dd").orElse("")).status(Status.ALREADY_REPORTED));
        
        filter(new LogRequestTimingFilter());
    }

    public static void main(String[] args) {
        run(JawnMainTest.class, args);
    }

}
