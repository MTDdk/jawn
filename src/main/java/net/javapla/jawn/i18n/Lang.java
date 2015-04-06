package net.javapla.jawn.i18n;

import net.javapla.jawn.PropertiesImpl;

import com.google.inject.Inject;


public class Lang {
    
    private final String[] languages;
    private final String default_language;
    
    /*public Lang(String[] languages) {
        this.languages = languages;//conf.getStringArray(Constants.SUPPORTED_LANGUAGES);
        this.default_language = initDefaultLanguage();
    }*/
    
    @Inject
    public Lang(PropertiesImpl properties) {
        this.languages = properties.getSupportedLanguages();
        this.default_language = initDefaultLanguage();
    }
    
    
    public boolean areLanguagesSet() {
        return default_language != null;
    }
    public String[] getLanguages() {
        return languages;
    }
    public String getDefaultLanguage() {
        return default_language;
    }
    public boolean isLanguageSupported(String lang) {
        if (languages != null) {
            for (String language : languages) {
                if (lang.equals(language)) return true;
            }
        }
        return false;
    }
    
    
    private String initDefaultLanguage() {
//        String[] arr = properties.getStringArray(Constants.SUPPORTED_LANGUAGES);
        
        if (languages == null || languages.length == 0) return null;
        
        // by convention, the first language is default
        return languages[0];
    }
    
//    public String deduceLanguageFromUri(String uri) {
//        
//    }

}
