package net.javapla.jawn.core.spi;

import net.javapla.jawn.core.FilterHandler.FilterBuilder;
import net.javapla.jawn.core.FilterHandler.SecureBuilder;

public interface Filters {

    FilterBuilder<Filter> add(Filter filter);
    SecureBuilder secureOnRole(String role);
}
