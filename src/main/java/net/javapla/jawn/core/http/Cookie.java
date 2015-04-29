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
package net.javapla.jawn.core.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Igor Polevoy
 */
public class Cookie {

    static Logger logger = LoggerFactory.getLogger(Cookie.class);

    private java.lang.String name;
    private java.lang.String value;

    private int maxAge = -1;
    private java.lang.String domain = null;
    private java.lang.String path = "/";
    private boolean secure;
    private boolean httpOnly;
    private int version;

    public Cookie(java.lang.String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Cookie(java.lang.String name, String value, boolean httpOnly) {
        this.name = name;
        this.value = value;
        this.httpOnly = httpOnly;
    }

    public void setMaxAge(int maxAge) {this.maxAge = maxAge;}

    public int getMaxAge() {return maxAge;}

    public void setPath(String path) {this.path = path;}

    public java.lang.String getPath() {return path;}

    public void setDomain(String domain) {this.domain = domain;}

    public java.lang.String getDomain() {return domain;}

    public void setSecure(boolean secure) {this.secure = secure;}

    public boolean isSecure() {return secure;}

    public String getName() { return name;}

    public void setValue(String value) {this.value = value;}

    public String getValue() { return value; }

    public int getVersion() { return version; }

    public void setVersion(int version) { this.version = version;}

    /**
     * Sets this cookie to be HTTP only.
     *
     * This will only work with Servlet 3
     */
    public void setHttpOnly(){httpOnly = true;}

    /**
     * Tells if a cookie HTTP only or not.
     *
     * This will only work with Servlet 3
     */
    public boolean isHttpOnly(){return httpOnly;}

    /**
     * Sets this cookie to be Http only or not
     */
    public void setHttpOnly(boolean httpOnly){
        this.httpOnly = httpOnly;
    }

    @Override
    public String toString() {
        return "Cookie{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", maxAge=" + maxAge +
                ", domain='" + domain + '\'' +
                ", path='" + path + '\'' +
                ", secure=" + secure +
                ", version=" + version +
                '}';
    }
}
