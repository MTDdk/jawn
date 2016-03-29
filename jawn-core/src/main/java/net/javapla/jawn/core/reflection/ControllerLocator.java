package net.javapla.jawn.core.reflection;

public class ControllerLocator {
    
    private final ClassLocator locator;

    public ControllerLocator(String packageToScan) {
        locator = new ClassLocator(packageToScan);
    }

}
