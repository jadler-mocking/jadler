/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.Request;
import org.hamcrest.Matcher;


/**
 * A {@link RequestMatcher} used for matching the request query string.
 */
public class QueryStringRequestMatcher extends RequestMatcher<String> {

    /**
     * Protected constructor useful only when subtyping. For creating instances of this class use 
     * {@link #requestQueryString(org.hamcrest.Matcher)} instead.
     * @param pred a predicate to be applied on the query string
     */
    protected QueryStringRequestMatcher(final Matcher<? super String> pred) {
        super(pred);
    }


    /**
     * Retrieves the query string value of the given request. The value is percent-encoded.
     * @param req request to retrieve the query string from
     * @return query string value:
     * <ul>
     *   <li>{@code null} for requests without a query string part (no <em>?</em> character)</li>
     *   <li>an empty string for request with an empty query string part (<em>?</em> is present but there is no actual
     *   query string value)</li>
     *   <li>query string value (without the leading <em>?</em> character) for requests with a query string part</li>
     * </ul>
     */
    @Override
    public String retrieveValue(final Request req) {
        return req.getURI().getRawQuery();
    }


    /**
     * {@inheritDoc} 
     */
    @Override
    protected String provideDescription() {
        return "query string is";
    }


    /**
     * Factory method to create new instance of this matcher.
     * @param pred a predicate to be applied on the request query string
     * @return new instance of this matcher
     */
    public static QueryStringRequestMatcher requestQueryString(final Matcher<? super String> pred) {
        return new QueryStringRequestMatcher(pred);
    }
}