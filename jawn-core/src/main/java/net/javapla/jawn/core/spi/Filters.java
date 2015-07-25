package net.javapla.jawn.core.spi;

import net.javapla.jawn.core.FiltersHandler.FilterBuilder;

public interface Filters {

    FilterBuilder<Filter> add(Filter filter);
//    SecureBuilder secureOnRole(String role);
}
