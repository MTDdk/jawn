package net.javapla.jawn.application;

import net.javapla.jawn.core.db.DatabaseConnections;

public interface ApplicationDatabaseBootstrap {

    void dbConnections(DatabaseConnections connections);
}
