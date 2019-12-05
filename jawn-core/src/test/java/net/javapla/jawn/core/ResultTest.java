package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class ResultTest {

    @Test
    public void resultToString() {
        assertThat(Results.ok().toString()).isEqualTo("status ["+Status.OK+"], type ["+MediaType.PLAIN+"], char [null], obj [null]");
        
        assertThat(Results.redirect("/test").toString()).isEqualTo("status ["+Status.FOUND+"], type ["+MediaType.PLAIN+"], char [null], obj [null]");
        assertThat(Results.moved("/test").contentType(MediaType.TEXT).toString()).isEqualTo("status ["+Status.MOVED_PERMANENTLY+"], type ["+MediaType.TEXT+"], char [null], obj [null]");
        assertThat(Results.seeOther("/test").toString()).isEqualTo("status ["+Status.SEE_OTHER+"], type ["+MediaType.PLAIN+"], char [null], obj [null]");
    }
    
    @Test
    public void contentTypeFromString() {
        Result result = Results.ok();
        assertThat(result.contentType).isEqualTo(MediaType.PLAIN);
        
        result.contentType("application/json");
        
        assertThat(result.contentType).isEqualTo(MediaType.JSON);
    }
    
    @Test
    public void correctContentTypes() {
        assertThat(Results.xml(new Object()).contentType).isEqualTo(MediaType.XML);
        assertThat(Results.json(new Object()).contentType).isEqualTo(MediaType.JSON);
        assertThat(Results.text(new Object()).contentType).isEqualTo(MediaType.PLAIN);
    }
    
    @Test
    public void multipleTextPieces() {
        assertThat(Results.text("test..{0}..{1}..{2}", "1","2", "3").renderable).isEqualTo("test..1..2..3".getBytes());
    }

}
