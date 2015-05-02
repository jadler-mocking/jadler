/*
 * Copyright (c) 2012 - 2015 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.Request;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;


/**
 * A {@link RequestMatcher} used for matching the request path.
 */
public class PathRequestMatcher extends RequestMatcher<String> {

    /**
     * Protected constructor useful only when subtyping. For creating instances of this class use
     * {@link #requestPath(org.hamcrest.Matcher)} instead.
     *
     * @param pred a predicate to be applied on the request path
     */
    protected PathRequestMatcher(final Matcher<? super String> pred) {
        super(pred);
    }


    /**
     * Retrieves the path of the given request. The value is percent-encoded.
     *
     * @param req request to retrieve the path from
     * @return request path (never returns {@code null})
     */
    @Override
    public String retrieveValue(final Request req) {
        return req.getURI().getRawPath();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String provideDescription() {
        return "Path is";
    }


    /**
     * Factory method to create new instance of this matcher.
     *
     * @param pred a predicate to be applied on the request path
     * @return new instance of this matcher
     */
    @Factory
    public static PathRequestMatcher requestPath(final Matcher<? super String> pred) {
        return new PathRequestMatcher(pred);
    }
}