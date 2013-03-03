/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.stubbing.Request;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;


/**
 * Implementation of <tt>RequestMatcher</tt> used for matching query string decoded from HTTP request.
 *
 */
public class QueryStringRequestMatcher extends RequestMatcher<String> {


    public QueryStringRequestMatcher(final Matcher<? super String> pred) {
        super(pred);
    }

    
    @Override
    public String retrieveValue(final Request req) throws Exception {
        return req.getQueryString();
    }
    
    
    @Override
    protected String provideDescription() {
        return "query string is";
    }


    @Factory
    public static QueryStringRequestMatcher requestQueryString(final Matcher<? super String> pred) {
        return new QueryStringRequestMatcher(pred);
    }
}