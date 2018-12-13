package net.javapla.jawn.core;

public class JawnMainTest extends Jawn {
    
    {
        get("/", Results.text("holaa"));
        get("/test", Results.text("teeeest").status(Status.ALREADY_REPORTED));
        get("/xml", Results.xml("<xml>teeeest</xml>"));
        get("/json", Results.json("{\"key\":\"teeeest\"}"));
    }

    public static void main(String[] args) {
        run(JawnMainTest.class, args);
    }

}
