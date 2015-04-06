package net.javapla.jawn;

public class JawnDefault {

    
    private final NewRouter router;
    private final ControllerResponseRunner runner;
    
    public JawnDefault(NewRouter router, ControllerResponseRunner runner) {
        this.router = router;
        this.runner = runner;
    }
    
    //onRouteRequest
    public void runRoute(Context context) {
        router.GET();
        runner.toString();
    }
}
