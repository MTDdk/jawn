package implementation;

import implementation.controllers.TestController;
import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.filters.LogRequestTimingFilter;
import net.javapla.jawn.core.server.ServerConfig.Performance;
import net.javapla.jawn.core.util.Modes;

public class JawnMainTest extends Jawn {
    
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
    }

    public static void main(String[] args) {
        run(JawnMainTest.class, args);
    }

}
