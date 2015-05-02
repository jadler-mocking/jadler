/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.Request;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;


/**
 * A {@link RequestMatcher} used for matching the request body as an array of bytes.
 */
public class RawBodyRequestMatcher extends RequestMatcher<byte[]> {


    /**
     * Protected constructor useful only when subtyping. For creating instances of this class use 
     * {@link #requestRawBody(org.hamcrest.Matcher)} instead.
     * @param pred a predicate to be applied on the request body
     */
    protected RawBodyRequestMatcher(final Matcher<byte[]> pred) {
        super(pred);        
    }

    
    /**
     * Retrieves the body of the given request
     * @param req request to retrieve the body from
     * @return request body as an array of bytes (never returns {@code null})
     */
    @Override
    protected byte[] retrieveValue(final Request req) throws Exception {
        return IOUtils.toByteArray(req.getBodyAsStream());
    }
    
    
    /**
     * {@inheritDoc} 
     */
    @Override
    protected String provideDescription() {
        return "raw body is";
    }


    /**
     * Factory method to create new instance of this matcher.
     * @param pred a predicate to be applied on the request body
     * @return new instance of this matcher
     */
    @Factory
    public static RawBodyRequestMatcher requestRawBody(final Matcher<byte[]> pred) {
        return new RawBodyRequestMatcher(pred);
    }
}
