package net.javapla.jawn.plugins.modules;

import static com.google.common.truth.Truth.assertThat;

import java.util.Map;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.zaxxer.hikari.HikariDataSource;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.spi.ApplicationConfig;
import net.javapla.jawn.core.util.CollectionUtil;
import net.javapla.jawn.core.util.Modes;
import net.javapla.jawn.database.DatabaseConfigurationReaderTest;

public class HikariBootstrapTest {
    
    static Injector injector;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        Map<String, String> map = CollectionUtil.map(
            "database.tfb.url",      "jdbc:postgresql://tfb-database:5432/hello_world",
            "database.tfb.driver",   "org.postgresql.Driver",
            "database.tfb.user",     "tfbuser",
            "database.tfb.password", "tfbuserpass",
            
            "database.userdb.url",      "jdbc:postgresql://userdb:5432/hello_world",
            "database.userdb.driver",   "org.postgresql.Driver",
            "database.userdb.user",     "userdbuser",
            "database.userdb.password", "userdbpass",
            
            "database.audit.url",      "jdbc:postgresql://audit:5432/hello_world",
            "database.audit.driver",   "org.postgresql.Driver",
            "database.audit.user",     "auditdbuser",
            "database.audit.password", "auditdbpass",
            
            // standard convention
            "database.url",      "jdbc:postgresql://db:5432/hello_world",
            "database.driver",   "org.postgresql.Driver",
            "database.user",     "dbuser",
            "database.password", "dbpass"
        );
        final Config config = DatabaseConfigurationReaderTest.createConfig(map);
        
        final com.google.inject.Module module = binder -> {
            ApplicationConfig applicationConfig = new ApplicationConfig() {
                @Override
                public Binder binder() { return binder; }
                @Override
                public Config configuration() { return config; }
    
                @Override
                public Modes mode() { return null; }
                @Override
                public void onStartup(Runnable task) {}
                @Override
                public void onShutdown(Runnable task) {}
            };
            
            // Actual setup..
            new HikariBootstrap().bootstrap(applicationConfig);
        };
        
        injector = Guice.createInjector(module);
    }

    @Test
    public void getInstance() {
        DataSource standard = injector.getInstance(Key.get(DataSource.class));
        assertThat(standard).isNotNull();
        assertThat(((HikariDataSource)standard).getJdbcUrl()).isEqualTo("jdbc:postgresql://db:5432/hello_world");
        
        
        DataSource tfb = injector.getInstance(Key.get(DataSource.class, Names.named("tfb")));
        assertThat(tfb).isNotNull();
        assertThat(((HikariDataSource)tfb).getJdbcUrl()).isEqualTo("jdbc:postgresql://tfb-database:5432/hello_world");
    }
    
    @Test
    public void injection() {
        InjectionTest instance = injector.getInstance(InjectionTest.class);
        
        assertThat(instance).isNotNull();
        assertThat(instance.userdb).isInstanceOf(HikariDataSource.class);
        assertThat(((HikariDataSource)instance.userdb).getUsername()).isEqualTo("userdbuser");
    }
    
    private static class InjectionTest {
        final DataSource userdb;

        @Inject
        InjectionTest(@Named("userdb") DataSource userdb) {
            this.userdb = userdb;
        }
    }

}
