package net.javapla.jawn.database;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.google.inject.name.Names;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.spi.ApplicationConfig;

public class DatabaseConfigurationReader  {
    
    static final String 
        DATABASE_KEY = "database",
        USER = "user",
        PASSWORD = "password",
        URL = "url",
        DRIVER = "driver",
        MAX_POOL_SIZE = "max_pool_size";
    
    static final List<String> KEYWORDS = Arrays.asList(new String[] {
        USER,PASSWORD,URL,DRIVER,MAX_POOL_SIZE
    });
    
    
    public static void bindInstances(ApplicationConfig appConfig, Function<DatabaseConnection, DataSource> source) {
        Config config = appConfig.configuration();
        Set<DatabaseConnection> databases = DatabaseConfigurationReader.namedDatabases(config);
        
        if (databases.size() == 1) {
            
            // should never be named
            appConfig.binder().bind(DataSource.class).toInstance(source.apply(databases.stream().findFirst().get()));
            
        } else {
            
            databases.forEach(db -> {
                DataSource dataSource = source.apply(db);
                
                if (!db.name().isPresent()) {
                    appConfig.binder().bind(DataSource.class).toInstance(dataSource);
                } else {
                    appConfig.binder().bind(DataSource.class).annotatedWith(Names.named(db.name().get())).toInstance(dataSource);
                }
            });
        }
    }
    
    
    // - database.user
    // - database.<name>.user
    public static Set<DatabaseConnection> namedDatabases(Config config) {
        
        // Are we dealing with multiple databases?
        // Find the names of each DB and create a DatabaseConnection per name
        Set<String> distinctDatabaseNames = config.keysOf(DATABASE_KEY)
            .stream()
            .map(s -> s.substring(DATABASE_KEY.length() +1)) // remove the 'database.'
            .filter(s -> s.indexOf('.') > 0) // filter out any standard convention keys (keys on the format of 'database.key', which are now only 'key')
            .filter(s -> KEYWORDS.stream().filter(s::endsWith).findAny().isPresent()) // only look for database names actually using known keywords
            .map(s -> s.substring(0, s.lastIndexOf('.'))) // filter out the keyword itself
            .distinct()
            .collect(Collectors.toSet());
        
        // We expect this to mean that there is a config for the standard convention
        if (config.hasPath(DATABASE_KEY + '.' + URL)) {
            distinctDatabaseNames.add("");
        }
        
        
        return distinctDatabaseNames
            .stream()
            .map(name -> connection(name, config))
            .collect(Collectors.toSet());
    }
    
    private static DatabaseConnection connection(final String name, final Config config) {
        final String key = DATABASE_KEY + '.' + (name.isEmpty() ? "" : name + '.');
        
        return new DatabaseConnection() {
            @Override
            public Optional<String> name() {
                return name.isEmpty() ? Optional.empty() : Optional.of(name);
            }

            @Override
            public Optional<String> url() {
                return config.getOptionally(key + URL);
            }

            @Override
            public Optional<String> driver() {
                return config.getOptionally(key + DRIVER);
            }

            @Override
            public Optional<String> user() {
                return config.getOptionally(key + USER);
            }

            @Override
            public Optional<String> password() {
                return config.getOptionally(key + PASSWORD);
            }

            @Override
            public Optional<Integer> maxPoolSize() {
                return config.getIntOptionally(key + MAX_POOL_SIZE);
            }
        };
    }
}
