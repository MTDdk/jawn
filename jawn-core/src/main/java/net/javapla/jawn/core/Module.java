package net.javapla.jawn.core;

public interface Module {
    
    
    void install(Application config);

    
    
    public static interface Application {
        
        Registry registry();
        
        Router router();
        
        void onStartup(Runnable task);
        
        void onShutdown(Runnable task);
    }
    
}
