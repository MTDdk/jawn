package implementation;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.Result;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Route;

public class SomeRandomClass {

    public static Result result1() {
        return Results.text("result");
    }
    
    public static void before(Context c) {
        System.out.println("some output");
        //return ch.next(c);
    }
}
