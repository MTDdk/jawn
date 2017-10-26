package net.javapla.jawn.core.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.javapla.jawn.core.crypto.SecretGenerator;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.Modes;

public abstract class ConfigurationsHelper {
    public static final Logger logger = LoggerFactory.getLogger(ConfigurationsHelper.class);

    public static void check(JawnConfigurations properties) {
        if (properties.getMode() == Modes.PROD) return;
        
        Optional<String> secure = properties.getSecure(Constants.PROPERTY_SECURITY_SECRET);
        if (!secure.isPresent()) {
            logger.info("Jawn is generating a secret for you at " + Constants.PROPERTIES_FILE_USER);
            
            String secret = SecretGenerator.generate();
            properties.set(Constants.PROPERTY_SECURITY_SECRET, secret);
            
            // try to save it
            Properties props = new Properties();
            props.setProperty(Constants.PROPERTY_SECURITY_SECRET, secret);
            File to = new File(new File("").getAbsolutePath() + "/src/main/resources/"+Constants.PROPERTIES_FILE_USER);
            
            try (FileOutputStream stream = new FileOutputStream(to, true);
                PrintWriter out = new PrintWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8))) {
                out.println();
                out.println(Constants.PROPERTY_SECURITY_SECRET+"="+secret);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
