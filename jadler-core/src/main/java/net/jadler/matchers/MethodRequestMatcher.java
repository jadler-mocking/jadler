/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import net.jadler.Request;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;


/**
 * A {@link RequestMatcher} used for matching the request method.
 */
public class MethodRequestMatcher extends RequestMatcher<String> {


    /**
     * Protected constructor useful only when subtyping. For creating instances of this class use 
     * {@link #requestMethod(org.hamcrest.Matcher)} instead.
     * @param pred a predicate to be applied on the request method
     */
    protected MethodRequestMatcher(final Matcher<? super String> pred) {
        super(pred);
    }


    /**
     * Retrieves the the method of the given request
     * @param req request to retrieve the method from
     * @return request method
     */
    @Override
    protected String retrieveValue(final Request req) {
        return req.getMethod();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String provideDescription() {
        return "method is";
    }


    /**
     * Factory method to create new instance of this matcher.
     * @param pred a predicate to be applied on the request method
     * @return new instance of this matcher
     */
    @Factory
    public static MethodRequestMatcher requestMethod(final Matcher<? super String> pred) {
        return new MethodRequestMatcher(pred);
    }
}