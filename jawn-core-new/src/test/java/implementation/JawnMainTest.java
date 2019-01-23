package implementation;

import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.filters.LogRequestTimingFilter;

public class JawnMainTest extends Jawn {
    
    {
        get("/", Results.text("holaaaa5588")).before(SomeRandomClass::before);
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
