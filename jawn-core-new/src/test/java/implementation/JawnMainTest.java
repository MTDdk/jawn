package implementation;

import java.util.LinkedList;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Jawn;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.Route.Chain;
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
        
        /*LinkedList<Object> bagOFilters = new LinkedList<>();
        bagOFilters.add(F.class);
        bagOFilters.add(B.class);
        bagOFilters.add(A.class);
        
        for (Object item : bagOFilters) {
            
            Class<?> d = (Class<?>)item;
            System.out.println(Route.Filter.class.isAssignableFrom(d));
            System.out.println(Route.After.class.isAssignableFrom(d));
            System.out.println(Route.Before.class.isAssignableFrom(d));
            System.out.println();
        }*/
        
    }
    
    public class F implements Route.Filter {
        @Override
        public Result before(Context context, Chain chain) {
            return null;
        }

        @Override
        public Result after(Context context, Result result) {
            return null;
        }
    }
    
    public class B implements Route.Before {
        @Override
        public Result before(Context context, Chain chain) {
            return null;
        }
    }
    
    public class A implements Route.After {
        @Override
        public Result after(Context context, Result result) {
            return null;
        }
    }

}
