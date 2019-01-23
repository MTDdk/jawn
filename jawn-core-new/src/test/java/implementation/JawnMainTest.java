package implementation;

import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Status;
import net.javapla.jawn.core.filters.LogRequestTimingFilter;

public class JawnMainTest extends Jawn {
    
    {
        get("/", Results.text("holaaaa5588")).before(SomeRandomClass::before);
        get("/xml", Results.xml("<xml>teeeest</xml>"));
        get("/json", Results.json("{\"key\":\"teeeest\"}"));
        
        get("/test", ctx -> Results.text("teeeest :: " + ctx.param("dd").orElse("")).status(Status.ALREADY_REPORTED));
        post("/test/{dd}", ctx -> Results.text("teeeest :: " + ctx.param("dd").orElse("")).status(Status.ALREADY_REPORTED));
        
        filter(LogRequestTimingFilter.class);
    }

    public static void main(String[] args) {
        run(JawnMainTest.class, args);
    }

}
