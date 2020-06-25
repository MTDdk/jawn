package net.javapla.jawn.core.renderers.template.config;

import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.Modes;
import net.javapla.jawn.core.View;

/**
 * Empirical evidence tells us that caching the SiteConfiguration has a significant impact on performance in high load use.
 * This makes sense as ALL roundtrips to disk have great cost.
 * 
 * @author MTD
 */
@Singleton
public class SiteProvider {

    private final Modes mode;
    private final boolean useCache;
    private final SiteConfigurationReader confReader;
    
    // README: ConcurrentHashMap might deadlock with the same hash - so do we really need the concurrency?
    // https://stackoverflow.com/questions/43861945/deadlock-in-concurrenthashmap
    private final HashMap<String, Site> cachedSiteObjs = new HashMap<>();
    
    @Inject
    public SiteProvider(final ObjectMapper mapper, final DeploymentInfo deploymentInfo, final Modes mode) {
        this.mode = mode;
        this.useCache = mode != Modes.DEV;
        this.confReader = new SiteConfigurationReader(mapper, deploymentInfo);
    }
    
    
    public Site load(Context ctx, View view, String content) {
        if (useCache) {
            Site site = cachedSiteObjs.computeIfAbsent(view.path(), path -> {
                SiteConfiguration conf = confReader.find(view.path());
                return cachable(conf);
            });
            
            return site.url(ctx.req().path()).content(content);
        }
        
        // Yes, yes... duplicated code
        
        SiteConfiguration conf = confReader.find(view.path());
        return cachable(conf).url(ctx.req().path()).content(content);
    }
    
    private Site cachable( SiteConfiguration conf) {
        return Site.builder(mode) // state the current mode
            //.url(ctx.req().path()) // add the URL <-- seems to be something we should not/can not cache
            .title(conf.title) // add title
            //add scripts
            .scripts(conf.scripts)
            .styles(conf.styles)
            .build();
    }
}
