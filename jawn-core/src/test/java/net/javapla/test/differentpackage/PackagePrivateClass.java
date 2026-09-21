package net.javapla.test.differentpackage;

import net.javapla.jawn.core.annotation.Singleton;

@Singleton
class PackagePrivateClass {

    public PackagePrivateClass() {}
    
    String returnSomething() {
        return "test_" + this.getClass().getSimpleName();
    }
}
