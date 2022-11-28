package net.javapla.jawn.core.internal;

class PathParser {
    
    private static final char PARAM_START = '{'; // /{param}
    
    static boolean wildcarded(String path) {
        return path.indexOf('{') > 0;
    }
    
    static void parse(String originalPath) {
        
    }

}
