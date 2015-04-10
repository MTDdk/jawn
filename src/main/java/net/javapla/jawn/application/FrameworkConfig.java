package net.javapla.jawn.application;

import net.javapla.jawn.ConfigApp;
import net.javapla.jawn.Filters;
import net.javapla.jawn.Router;

public abstract class FrameworkConfig {

    public void bootstrap(ConfigApp config){}
    public void router(Router router){}
    public void filters(Filters filters){}
    
    public void destroy(){}
}
