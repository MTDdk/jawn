package net.javapla.jawn.database;

import java.util.Optional;

public interface DatabaseConnection {
    Optional<String> name();

    Optional<String> url();
    Optional<String> driver();
    Optional<String> user();
    Optional<String> password();
    
    Optional<Integer> maxPoolSize();
    
}
