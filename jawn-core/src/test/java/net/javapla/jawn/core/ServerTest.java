package net.javapla.jawn.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import net.javapla.jawn.core.Server.ServerConfig;

class ServerTest {

    @Test
    void fromConfig() {
        Config config = ConfigFactory.parseString("{something:nothing,server.port=8082}");
        ServerConfig serverConfig = Server.ServerConfig.from(config.getConfig("server"));
        assertEquals(8082,serverConfig.port());
    }
    
    @Test
    void overrideDefaultConfig() {
        Server.ServerConfig conf = new Server.ServerConfig();
        conf.config(ConfigFactory.parseString("{server.port=8033}"));
        
        assertEquals(8033, conf.port());
    }

}
