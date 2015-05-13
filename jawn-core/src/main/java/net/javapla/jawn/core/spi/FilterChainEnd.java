package net.javapla.jawn.core.spi;

/**
 * Indicates the end of a filter chain.
 * This should always be the last filter to be called.
 * 
 * This is also the caller of the controller action.
 * 
 * @author MTD
 */
public interface FilterChainEnd extends FilterChain {

}
