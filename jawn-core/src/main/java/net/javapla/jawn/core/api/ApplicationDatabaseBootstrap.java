package net.javapla.jawn.core.api;

import net.javapla.jawn.core.database.DatabaseConnections;

//TODO might be possible to have as an interface within ApplicationBootstrap instead
public interface ApplicationDatabaseBootstrap {

    void dbConnections(DatabaseConnections connections);
}
