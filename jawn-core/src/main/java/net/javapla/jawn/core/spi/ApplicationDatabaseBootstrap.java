package net.javapla.jawn.core.spi;

import net.javapla.jawn.core.database.DatabaseConnections;

public interface ApplicationDatabaseBootstrap {

    void dbConnections(DatabaseConnections connections);
}
