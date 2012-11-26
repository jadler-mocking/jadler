/*
 * Copyright (c) 2012 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import javax.servlet.http.HttpServletRequest;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;


/**
 * Implementation of <tt>RequestMatcher</tt> used for matching HTTP method name.
 *
 */
public class MethodRequestMatcher extends RequestMatcher<String> {


    public MethodRequestMatcher(final Matcher<? super String> pred) {
        super(pred);
    }


    @Override
    protected String retrieveValue(final HttpServletRequest req) throws Exception {
        return req.getMethod();
    }
    
    
    @Override
    protected String provideDescription() {
        return "method is";
    }


    @Factory
    public static MethodRequestMatcher requestMethod(final Matcher<? super String> pred) {
        return new MethodRequestMatcher(pred);
    }
}