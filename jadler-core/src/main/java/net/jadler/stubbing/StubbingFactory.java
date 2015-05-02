/*
 * Copyright (c) 2012-2014 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import java.nio.charset.Charset;
import org.apache.commons.collections.MultiMap;


/**
 * Factory class for creating {@link Stubbing} instances.
 */
public class StubbingFactory {
    
    /**
     * Creates new stubbing instance.
     * @param defaultEncoding default encoding of every stub response body
     * @param defaultStatus default http status of every stub response
     * @param defaultHeaders default response headers of every stub response
     * @return new {@link Stubbing} instance
     */
    public Stubbing createStubbing(final Charset defaultEncoding, final int defaultStatus,
            final MultiMap defaultHeaders) {
        
        return new Stubbing(defaultEncoding, defaultStatus, defaultHeaders);
    }
}
