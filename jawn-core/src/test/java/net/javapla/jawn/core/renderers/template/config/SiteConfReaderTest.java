package net.javapla.jawn.core.renderers.template.config;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Correspondence;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.parsers.JsonMapperProvider;
import net.javapla.jawn.core.util.Constants;

public class SiteConfReaderTest {
    
    private static final Path resources = Paths.get("src", "test", "resources", "renderers", "template", "config"); 
    
    static ObjectMapper objectMapper;
    static Config config;
    static SiteConfReader confReader;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        objectMapper = new JsonMapperProvider().get();
        
        config = mock(Config.class);
        when(config.getOptionally(Constants.PROPERTY_DEPLOYMENT_INFO_WEBAPP_PATH)).thenReturn(Optional.of(resources.toString()));
        DeploymentInfo di = new DeploymentInfo(config, StandardCharsets.UTF_8, "");
        
        confReader = new SiteConfReader(objectMapper, di);
    }

    @Test
    public void readSiteFile() {
        SiteConfiguration conf = confReader.readSiteFile("");
        
        assertThat(conf.title).isEqualTo("jawn test");
        assertThat(conf.scripts).hasLength(2);
        assertThat(conf.styles).hasLength(2);
        
        assertThat(conf.scripts)
            .asList()
            .comparingElementsUsing(Correspondence.transforming((SiteConfiguration.Tag tag) -> tag.url, "has url"))
            .containsExactly("/js/script1.js", "/js/script2.js");
        
        assertThat(conf.styles)
            .asList()
            .comparingElementsUsing(Correspondence.transforming((SiteConfiguration.Tag tag) -> tag.url, "has url"))
            .containsExactly("/css/style1.css", "/css/style2.css");
        
        assertThat(conf.styles[1].attr).containsExactly("integrity","#2",   "crossorigin", "none");
    }
    
    @Test
    public void readSiteFile_handleSlash() {
        SiteConfiguration conf = confReader.readSiteFile("/");
        
        assertThat(conf.title).isEqualTo("jawn test");
        assertThat(conf.scripts).hasLength(2);
        assertThat(conf.styles).hasLength(2);
    }
    
    @Test
    public void override() {
        SiteConfiguration conf = confReader.find("override");
        
        assertThat(conf).isNotNull();
        assertThat(conf.overrideDefault).isTrue();
        assertThat(conf.title).isEqualTo("jawn test overridden");
        assertThat(conf.scripts).hasLength(1);
        assertThat(conf.styles).isNull(); //.hasLength(0);
        
        assertThat(conf.scripts[0].url).isEqualTo("/js/script3.js");
    }

    @Test
    public void merge() {
        // "first" is the base, and "second" will override the title, if it has any
        // All Tags will be merged
        SiteConfiguration first = confReader.readSiteFile("");
        SiteConfiguration second = confReader.readSiteFile("mergable");
        
        SiteConfiguration merge = confReader.merge(first, second);
        
        assertThat(merge.title).isEqualTo(second.title);
        assertThat(merge.scripts).hasLength(first.scripts.length + second.scripts.length);
        assertThat(merge.styles).hasLength(first.styles.length/* + second.styles.length*/); // "second" has no styles
    }
    
    @Test
    public void merge_should_not_alterCachedVersions() {
        SiteConfiguration first = confReader.readSiteFile("");
        SiteConfiguration second = confReader.readSiteFile("mergable");
        confReader.merge(first, second);
        
        assertThat(first.title).isNotEqualTo(second.title);
        assertThat(first.scripts.length).isNotEqualTo(second.scripts.length);
        assertThat(second.styles).isNull(); // as it where to begin with
        assertThat(first.styles).hasLength(2);
    }
    
    @Test
    public void find_should_not_mergeWithItself() {
        // Had an issue if "path" is the same as "root", 
        // the method would merge the same file into a new one,
        // which of course resulted in doubled Tags
        
        SiteConfiguration conf = confReader.find("");
        
        assertThat(conf.scripts).hasLength(2);
        assertThat(conf.styles).hasLength(2);
    }
    
    @Test
    public void contextPath() {
        String context = "/somecontextpath";
        DeploymentInfo di = new DeploymentInfo(config, StandardCharsets.UTF_8, context);
        
        SiteConfReader contextPathConfReader = new SiteConfReader(objectMapper, di);
        SiteConfiguration conf = contextPathConfReader.readSiteFile("");
        
        assertThat(conf.scripts)
            .asList()
            .comparingElementsUsing(Correspondence.transforming((SiteConfiguration.Tag tag) -> tag.url, "has url"))
            .containsExactly(context + "/js/script1.js", context +"/js/script2.js");
        
        assertThat(conf.styles)
            .asList()
            .comparingElementsUsing(Correspondence.transforming((SiteConfiguration.Tag tag) -> tag.url, "has url"))
            .containsExactly(context + "/css/style1.css", context + "/css/style2.css");
    }
    
    @Test
    public void clone_should_includeAttributes() {
        SiteConfiguration conf = confReader.readSiteFile("");
        
        SiteConfiguration clone = conf.clone();
        
        assertThat(conf.styles[1].attr).hasSize(clone.styles[1].attr.size());
        assertThat(conf.styles[1].attr).containsExactlyEntriesIn(clone.styles[1].attr);
    }
    
    @Test
    public void nonTranslatableLinks() {
        SiteConfiguration conf = confReader.find("nontranslatable");
        
        assertThat(conf.title).isEqualTo("jawn test non-translatable");
        assertThat(conf.scripts).hasLength(4);
        
        // should not translate the urls and prepend with /js/ or /css/
        assertThat(conf.scripts)
            .asList()
            .comparingElementsUsing(Correspondence.transforming((SiteConfiguration.Tag tag) -> tag.url, "has url"))
            .doesNotContain("/js/");
    }
    
    @Test
    public void isLocal() {
        assertThat(SiteConfReader.isLocal("")).isTrue();
        assertThat(SiteConfReader.isLocal("http://something.com")).isFalse();
        assertThat(SiteConfReader.isLocal("https://something.com")).isFalse();
        assertThat(SiteConfReader.isLocal("ftp://something.com")).isFalse();
        assertThat(SiteConfReader.isLocal("ftps://something.com")).isFalse();
        assertThat(SiteConfReader.isLocal("//something.com")).isFalse();
        assertThat(SiteConfReader.isLocal("file://something")).isTrue();
        assertThat(SiteConfReader.isLocal("something.css")).isTrue();
        assertThat(SiteConfReader.isLocal("something.js")).isTrue();
    }
    
    public void faulty() {
        //TODO
        // add test that catches or tells the user whenever there is an error when reading site.json
        // like in SiteConfiguration conf = confReader.readSiteFile("faulty");
    }
}
