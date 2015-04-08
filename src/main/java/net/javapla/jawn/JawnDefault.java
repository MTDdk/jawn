package net.javapla.jawn;

public class JawnDefault {

    
    private final Router router;
    private final ResponseRunner runner;
    
    public JawnDefault(Router router, ResponseRunner runner) {
        this.router = router;
        this.runner = runner;
    }
    
    //onRouteRequest
    public void runRoute(Context context) {
        router.GET();
        runner.toString();
    }
}
