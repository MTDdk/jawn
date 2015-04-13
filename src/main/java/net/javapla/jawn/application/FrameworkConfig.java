package net.javapla.jawn.application;

import net.javapla.jawn.ConfigApp;
import net.javapla.jawn.Filters;
import net.javapla.jawn.Router;
import net.javapla.jawn.db.DatabaseConnections;

public abstract class FrameworkConfig {

    public void bootstrap(ConfigApp config){}
    public void router(Router router){}
    public void filters(Filters filters){}
    public void dbConnections(DatabaseConnections connections) {}
    
    public void destroy(){}
}
