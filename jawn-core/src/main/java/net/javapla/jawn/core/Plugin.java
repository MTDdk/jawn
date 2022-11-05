package net.javapla.jawn.core;

public interface Plugin {
    
    
    void install(Application config);

    
    
    public static interface Application {
        
        Registry.ServiceRegistry registry();
        
        Router router();
        
        void renderer(MediaType type, Renderer renderer);
        
        void parser(MediaType type, Parser parser);
        
        void onStartup(Runnable task);
        
        void onShutdown(Runnable task);
    }
    
}
