/*
 * Copyright (c) 2012 - 2016 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.Request;
import org.hamcrest.Matcher;


/**
 * A {@link RequestMatcher} used for matching the request body as a string.
 */
public class BodyRequestMatcher extends RequestMatcher<String> {


    /**
     * Protected constructor useful only when subtyping. For creating instances of this class use
     * {@link #requestBody(org.hamcrest.Matcher)} instead.
     *
     * @param pred a predicate to be applied on the request body
     */
    protected BodyRequestMatcher(final Matcher<? super String> pred) {
        super(pred);
    }

    /**
     * Factory method to create new instance of this matcher.
     *
     * @param pred a predicate to be applied on the request body
     * @return new instance of this matcher
     */
    public static BodyRequestMatcher requestBody(final Matcher<? super String> pred) {
        return new BodyRequestMatcher(pred);
    }

    /**
     * Retrieves the body of the given request
     *
     * @param req request to retrieve the body from
     * @return request body as a string (never returns {@code null})
     */
    @Override
    protected String retrieveValue(final Request req) {
        return req.getBodyAsString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String provideDescription() {
        return "body is";
    }
}
