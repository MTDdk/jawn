/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/

package net.javapla.jawn.trash.controller_filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.javapla.jawn.Context;

/**
 * @author Igor Polevoy
 */
@Deprecated
public abstract class AbstractLoggingFilter extends ControllerFilterAdapter {
    protected Logger log = LoggerFactory.getLogger(getClass());
    
    private Level level;
    
    public enum Level {
        INFO, WARNING, DEBUG, ERROR, DISABLED
    }


    /**
     * Creates a filter with preset log level.
     *
     * @param level log level
     */
    public AbstractLoggingFilter(Level level) {
        this.level = level;
    }

    /**
     * Creates a filter with default "INFO" level.
     */
    public  AbstractLoggingFilter() {
        this(Level.INFO);
    }

    /**
     * Sets a log level at run time if needed.
     *
     * @param level log level
     */
    public void logAtLevel(Level level) {
        this.level = level;
    }

    @Override
    public void before(Context context) {
//        log("** Request headers **" + System.getProperty("line.separator") + getMessage(context));
        log(getMessage(context));
    }
    
    @Override
    public void after(Context context) {
        log(getMessage(context));
    }

    protected void log(String message){
        if (level.equals(Level.DISABLED)) {
            return;
        }
        if (level.equals(Level.INFO)) {
            log.info(message);
        }
        if (level.equals(Level.WARNING)) {
            log.warn(message);
        }
        if (level.equals(Level.DEBUG)) {
            log.debug(message);
        }
        if (level.equals(Level.ERROR)) {
            log.error(message);
        }
    }

    public Level getLevel() {
        return level;
    }

    protected abstract String getMessage(Context context);
}
