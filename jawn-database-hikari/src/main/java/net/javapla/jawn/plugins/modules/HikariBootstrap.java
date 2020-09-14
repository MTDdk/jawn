package net.javapla.jawn.plugins.modules;

import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

import ch.qos.logback.classic.Level;
import net.javapla.jawn.core.spi.ApplicationConfig;
import net.javapla.jawn.core.spi.ModuleBootstrap;
import net.javapla.jawn.database.DatabaseConfigurationReader;
import net.javapla.jawn.database.DatabaseConnection;

public class HikariBootstrap implements ModuleBootstrap {
    
    /** Minimum connection pool size. */
    private static final int MINIMUM_POOL_SIZE = 10;
    

    @Override
    public void bootstrap(ApplicationConfig appConfig) {
        
        // Not everything is necessary
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("com.zaxxer.hikari");
        logger.setLevel(Level.INFO);
        
        DatabaseConfigurationReader.bindInstances(appConfig, this::source);
        
    }
    
    private HikariDataSource source(DatabaseConnection db) {
        //Class.forName(driver());
        
        HikariDataSource source = new HikariDataSource();
        source.setDriverClassName(db.driver().get()); // <-- Optional.get is where implementors actually ought to fool proof the reading of configurations
        source.setJdbcUrl(db.url().get());
        source.setUsername(db.user().get());
        source.setPassword(db.password().get());
        
        source.setMaximumPoolSize(db.maxPoolSize().orElse(Math.max(MINIMUM_POOL_SIZE, Runtime.getRuntime().availableProcessors() * 2 + 1)));
        
        return source;
    }

}
