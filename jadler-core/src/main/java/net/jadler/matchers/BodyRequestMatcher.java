/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.Request;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * Implementation of <tt>RequestMatcher</tt> used for matching request body decoded from HTTP request.
 */
public class BodyRequestMatcher extends RequestMatcher<String> {


    public BodyRequestMatcher(final Matcher<? super String> pred) {
        super(pred);
    }

    
    @Override
    protected String retrieveValue(final Request req) throws Exception {
        return req.getBodyAsString();
    }
    
    

    @Override
    protected String provideDescription() {
        return "body is";
    }


    @Factory
    public static BodyRequestMatcher requestBody(final Matcher<? super String> pred) {
        return new BodyRequestMatcher(pred);
    }
}
