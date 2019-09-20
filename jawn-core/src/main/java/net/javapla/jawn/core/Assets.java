package net.javapla.jawn.core;

import java.time.Duration;

import net.javapla.jawn.core.util.TimeUtil;

public interface Assets {
    
    final class Impl implements Assets {
        public boolean etag = true;
        public boolean lastModified = false;
        public long maxAge = -1;
        
        @Override
        public Assets etag(boolean activate) {
            this.etag = activate;
            return this;
        }
        
        @Override
        public Assets lastModified(boolean activate) {
            this.lastModified = activate;
            return this;
        }
        
        @Override
        public Assets maxAge(long seconds) {
            this.maxAge = seconds;
            return this;
        }
    }

    Assets etag(boolean activate);
    
    Assets lastModified(boolean activate);

    Assets maxAge(long seconds);
    
    /**
     * Default.
     * One week - Google recommendation
     * https://developers.google.com/web/fundamentals/performance/optimizing-content-efficiency/http-caching#cache-control
     */
    default Assets maxAge() {
        return maxAge(604800);
    }
    
    default Assets maxAge(Duration duration) {
        return maxAge(duration.getSeconds());
    }
    
    /**
     * @param duration {@link TimeUtil#seconds(String)}
     * @return
     */
    default Assets maxAge(String duration) {
        return maxAge(TimeUtil.seconds(duration));
    }
}