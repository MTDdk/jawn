package net.javapla.jawn.core.database;


/**
 * Interface implemented by components that utilize a DatabaseConnection and wish that DatabaseConnection to be supplied if
 * one is available.
 *
 * <p>Currently only works for Filters
 */
public interface DatabaseConnectionAware {
    
    /**
     * Sets the available DatabaseConnection instance on this component.
     *
     * @param databaseConnection the DatabaseConnection instance to set on this component.
     */
    void setDatabaseConnection(DatabaseConnection databaseConnection);
}
