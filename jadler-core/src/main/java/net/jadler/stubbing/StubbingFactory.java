package net.jadler.stubbing;

import java.nio.charset.Charset;
import org.apache.commons.collections.MultiMap;


/**
 * Used internally, instances should never be created from outside.
 */
public class StubbingFactory {
    
    /**
     * @param defaultContentType default content type of every stub http response (null or empty string
     * for none default value)
     * @param defaultEncoding default encoding of every stub http response
     * @param defaultStatus default http status to be returned with every stub http response
     * @param defaultHeaders default response headers to be returned with every stub http response
     * @return new Stubbing instance.
     */
    public Stubbing createStubbing(final Charset defaultEncoding, final int defaultStatus,
            final MultiMap defaultHeaders) {
        
        return new Stubbing(defaultEncoding, defaultStatus, defaultHeaders);
    }
}
