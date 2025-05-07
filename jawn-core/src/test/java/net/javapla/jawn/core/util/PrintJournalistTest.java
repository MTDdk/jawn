package net.javapla.jawn.core.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class PrintJournalistTest {

    @Test
    void encoding() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintJournalist journalist = new PrintJournalist(outputStream, StandardCharsets.UTF_8);
        journalist.write("François");
        journalist.write('ç');
        "Tomáš".chars().forEach(c -> {
            try {
                journalist.write(c);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        journalist.close();
        
        //System.out.println(outputStream.toString());
        assertEquals("FrançoisçTomáš", outputStream.toString());
    }

}
