package net.javapla.jawn.core.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

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
            //new File("").getAbsolutePath()
            //ConfigurationsHelper.class.getClassLoader().getResource(Constants.PROPERTIES_FILE_USER).toExternalForm();
            Properties props = new Properties();
            props.setProperty(Constants.PROPERTY_SECURITY_SECRET, secret);
            File to = new File(new File("").getAbsolutePath() + "/src/main/resources/"+Constants.PROPERTIES_FILE_USER);
            try {
                Files.append(Constants.PROPERTY_SECURITY_SECRET+"="+secret, to, Charsets.UTF_8);
            } catch (IOException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }
            
            try (FileOutputStream stream = new FileOutputStream(to, true)) {
                
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

}
