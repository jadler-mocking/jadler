package net.jadler.stubbing;

import org.apache.commons.collections.MultiMap;


/**
 * Used internally, instances should never be created from outside.
 */
public class StubbingFactory {
    
    /**
     * @param defaultHeaders default response headers to be returned with every mock response
     * @param defaultStatus default http status to be returned with every mock response
     * @return new Stubbing instance.
     */
    public Stubbing createStubbing(final MultiMap defaultHeaders, final int defaultStatus) {
        return new Stubbing(defaultHeaders, defaultStatus);
    }
}
