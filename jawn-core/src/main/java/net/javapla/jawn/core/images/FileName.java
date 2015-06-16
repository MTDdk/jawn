package net.javapla.jawn.core.images;

import java.io.File;
import java.util.function.UnaryOperator;

/**
 * *Might need to be moved to util*
 * @author MTD
 */
class FileName {
    private static final char PATH_SEPERATOR = File.separatorChar, EXTENSION_SEPERATOR = '.';
    
    private static final char LEFT = '_';
    private static final char RIGHT = 'v';
    
    
    private String path;
    private String filename;
    private String ext;
    
    FileName() {}
    void setFile(String path) {
        init(path);
    }

    public FileName(String fullPath) {
        init(fullPath);
    }
    
    private void init(String fullPath) {
        initNameAndExtension(fullPath);
        
        //path
        this.path = path(fullPath); 
    }
    private void initNameAndExtension(String fullPath) {
        //extension
        this.ext = extension(fullPath);
        
        //filename
        this.filename = name(fullPath, this.ext.length());
    }
    private String path(String fullPath) {
        //path
        int sep = fullPath.lastIndexOf(PATH_SEPERATOR);
        return  (sep < 0) ? "" : fullPath.substring(0, sep);
    }
    
    public static String extension(String filename) {
        //extension
        int dot = filename.lastIndexOf(EXTENSION_SEPERATOR);
        if (dot < 1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }
    public static String extension(File file) {
        return extension(file.getName());
    }
    
    /**
     * @param filename
     * @return The name of the file without path and extension
     */
    public static String name(String filename) {
        //extension
        String ext = extension(filename);
        return name(filename, ext.length());
    }
    private static String name(String filename, int extensionLength) {
        //filename
        int sep = filename.lastIndexOf(PATH_SEPERATOR);
        int end = extensionLength == 0 ? filename.length() : filename.length() - extensionLength - 1;
        return filename.substring(sep + 1, end);
    }
    
    

    public String extension() {
        return this.ext;
    }

    /**
     * gets filename without extension
     * @return
     */
    public String filename() {
        return this.filename;
    }

    public String path() {
        return this.path;
    }
    
    public void updateName(String filename) {
        //fullPath = path() + pathSeparator + filename + extensionSeparator + extension();
//        this.filename = filename;
        this.filename = name(filename);
    }
    
    public void updateNameAndExtension(String file) {
        initNameAndExtension(file);
    }
    
    /**
     * To be characterised as a path, the <code>path</code> has to end with a slash /
     * @param path
     */
    public void updatePath(String path) {
        if (!path.contains(".") && !path.endsWith("/"))
            path += '/';
        this.path = path(path);
    }
    
    public String appendToFilename(String appendage) {
        this.filename += appendage;
        return this.filename + EXTENSION_SEPERATOR + ext;
    }
    
    public String fullPath() {
        return (path == null || path.isEmpty() ? "" : path + PATH_SEPERATOR) + filename + EXTENSION_SEPERATOR + ext;
    }
    
    /**
     * Apply the function to all string
     * @param function
     */
    public void apply(UnaryOperator<String> function) {
        this.filename = function.apply(this.filename);
        if (path != null && !path.isEmpty())
            this.path = function.apply(this.path);
    }
    
    public void sanitise() {
        apply((s) -> s.replace(' ', '_')); // replace space with underscore
        
        /*
         * Explanation:
         * [a-zA-Z0-9\\._] matches a letter from a-z lower or uppercase, numbers, dots and underscores
         * [^a-zA-Z0-9\\._] is the inverse. i.e. all characters which do not match the first expression
         * [^a-zA-Z0-9\\._]+ is a sequence of characters which do not match the first expression
         * So every sequence of characters which does not consist of characters from a-z, 0-9 or . _ will be replaced.
         */
        apply(s -> s.replaceAll("[^a-zA-Z0-9\\._]+", "_"));
    }
    
    /**
     * filename.jpg -&gt; filename_1v.jpg -&gt; filename_2v.jpg
     * 
     * I wanted to use filename[1].jpg, but ResourceHandler seems to not understand that format
     * 
     * @return The new filename
     */
    public String increment() {
        // filename[count].jpg
        int count = 0;
        String newFilename,
               altered = filename();
        if (altered.charAt(altered.length()-1) == RIGHT) { //(last char == 'v') we assume that a suffix already has been applied
            
            int rightBracket = altered.length()-1, 
                leftBracket  = altered.lastIndexOf(LEFT);
//              leftBracket  = rightBracket-2;// '-2' because at least one char ought to be between LEFT & RIGHT
                // crawling backwards
//              while (leftBracket > 0 && altered.charAt(leftBracket) != LEFT) leftBracket--;
            
            try {
                count = Integer.parseInt(altered.substring(leftBracket+1, rightBracket));
            } catch (NumberFormatException | IndexOutOfBoundsException ignore) {
                //if it fails, we can assume that it was not a correct suffix, so we add one now
                newFilename = altered + LEFT;
            }
            
            newFilename = altered.substring(0, leftBracket+1);
        } else {
            newFilename = altered + LEFT;
        }
        newFilename += (count+1);
        newFilename += RIGHT; // to prevent the char being added as an int
            
        updateName(newFilename);
        return newFilename + EXTENSION_SEPERATOR + ext;
    }
}