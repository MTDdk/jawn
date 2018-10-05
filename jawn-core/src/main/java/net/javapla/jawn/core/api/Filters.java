package net.javapla.jawn.core.api;

import com.google.inject.Injector;

import net.javapla.jawn.core.FiltersHandler.FilterBuilder;

public interface Filters {

    FilterBuilder<Filter> add(Filter filter);
    FilterBuilder<Filter> add(Class<? extends Filter> filter);
    void initialiseFilters(Injector injector);
//    SecureBuilder secureOnRole(String role);
}
