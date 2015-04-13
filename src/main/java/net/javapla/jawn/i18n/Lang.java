package net.javapla.jawn.i18n;

import net.javapla.jawn.PropertiesImpl;

import com.google.inject.Inject;

/**
 * 
 * @author MTD
 */
public class Lang {
    
    private final String[] languages;
    private final String default_language;
    
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
    
    
    /**
     * Finds the language segment of the URI if the language property is set.
     * Just looks at the first segment.
     * <p>
     * Fails fast if languages are not set
     * 
     * @param uri 
     *      The full URI
     * @return 
     *      the extracted language segment.
     * @throws LanguagesNotSetException 
     *      If {@link #areLanguagesSet()} returns false
     * @throws NotSupportedLanguageException 
     *      If the extracted language is not on the list of supported languages provided by the user
     */
    public String deduceLanguageFromUri(String uri) throws LanguagesNotSetException, NotSupportedLanguageException {
        if ( ! areLanguagesSet()) throw new LanguagesNotSetException();
        
        int start = uri.charAt(0) == '/' ? 1 : 0,
            end   = uri.indexOf('/', 1);
        
        if (end < 1) end = uri.length();
        
        String lang = uri.substring(start, end);
        
        if(isLanguageSupported(lang)) return lang;
        throw new NotSupportedLanguageException();
    }

}
