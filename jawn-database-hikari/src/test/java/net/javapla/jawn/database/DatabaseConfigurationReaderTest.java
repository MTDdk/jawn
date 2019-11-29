package net.javapla.jawn.database;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.mockito.AdditionalAnswers;

import com.google.common.truth.Correspondence;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.util.CollectionUtil;

public class DatabaseConfigurationReaderTest {

    @Test
    public void multipleConfigurations() {
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
        
        Config config = createConfig(map);
        Set<DatabaseConnection> set = DatabaseConfigurationReader.namedDatabases(config);
        
        assertThat(set).hasSize(4);
        
        assertThat(set)
            .comparingElementsUsing(CONNECTION_NAME)
            .containsExactly(Optional.empty(),Optional.of("tfb"),Optional.of("audit"),Optional.of("userdb"));
    }
    
    @Test
    public void standardConvention() {
        
        Map<String, String> map = CollectionUtil.map(
            // standard convention
            "database.url",      "jdbc:postgresql://db:5432/hello_world",
            "database.driver",   "org.postgresql.Driver",
            "database.user",     "dbuser",
            "database.password", "dbpass"
        );
        
        Config config = createConfig(map);
        Set<DatabaseConnection> set = DatabaseConfigurationReader.namedDatabases(config);
        
        assertThat(set).hasSize(1);
        
        
        DatabaseConnection connection = set.stream().findFirst().get();
        assertThat(connection.name().isPresent()).isFalse();
        assertThat(connection.url().get()).isEqualTo("jdbc:postgresql://db:5432/hello_world");
        assertThat(connection.driver().get()).isEqualTo("org.postgresql.Driver");
        assertThat(connection.user().get()).isEqualTo("dbuser");
        assertThat(connection.password().get()).isEqualTo("dbpass");
    }
    
    @Test
    public void noStandard() {
        Map<String, String> map = CollectionUtil.map(
            "database.tfb.url",      "jdbc:postgresql://tfb-database:5432/hello_world",
            "database.tfb.driver",   "org.postgresql.Driver",
            "database.tfb.user",     "tfbuser",
            "database.tfb.password", "tfbuserpass",
            
            "database.tfb.notavalidkeyword", "nonsense"
        );
        
        Config config = createConfig(map);
        Set<DatabaseConnection> set = DatabaseConfigurationReader.namedDatabases(config);
        
        assertThat(set).hasSize(1);
        
        
        DatabaseConnection connection = set.stream().findFirst().get();
        assertThat(connection.name().get()).isEqualTo("tfb");
        assertThat(connection.url().get()).isEqualTo("jdbc:postgresql://tfb-database:5432/hello_world");
        assertThat(connection.driver().get()).isEqualTo("org.postgresql.Driver");
        assertThat(connection.user().get()).isEqualTo("tfbuser");
        assertThat(connection.password().get()).isEqualTo("tfbuserpass");
    }
    
    
    public static Config createConfig(Map<String, String> map) {
        Config config = mock(Config.class);
        
        when(config.get(anyString())).thenAnswer(AdditionalAnswers.answer(map::get));
        when(config.getOptionally(anyString())).thenCallRealMethod();
        when(config.keys()).thenReturn(map.keySet());
        when(config.keysOf(anyString())).thenCallRealMethod();
        when(config.hasPath(anyString())).thenCallRealMethod();
        
        return config;
    }

    private final Correspondence<DatabaseConnection, Optional<String>> CONNECTION_NAME = Correspondence.transforming(DatabaseConnection::name, "has name");

}
