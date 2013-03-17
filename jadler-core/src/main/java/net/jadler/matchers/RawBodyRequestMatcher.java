/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.stubbing.Request;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;


public class RawBodyRequestMatcher extends RequestMatcher<byte[]> {


    public RawBodyRequestMatcher(final Matcher<byte[]> pred) {
        super(pred);        
    }

    
    @Override
    protected byte[] retrieveValue(final Request req) throws Exception {
        return IOUtils.toByteArray(req.getBody());
    }
    
    

    @Override
    protected String provideDescription() {
        return "raw body is";
    }


    @Factory
    public static RawBodyRequestMatcher requestRawBody(final Matcher<byte[]> pred) {
        return new RawBodyRequestMatcher(pred);
    }
}
