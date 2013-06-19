/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import java.util.List;

import net.jadler.Request;
import org.apache.commons.lang.Validate;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;


/**
 * A {@link RequestMatcher} used for matching a request header.
 */
public class HeaderRequestMatcher extends RequestMatcher<List<String>> {

    private final String headerName;
    private final String desc;


    /**
     * Protected constructor useful only when subtyping. For creating instances of this class use 
     * {@link #requestHeader(java.lang.String, org.hamcrest.Matcher)} instead.
     * @param pred a predicate to be applied on the given request header
     * @param headerName name of a request header (case insensitive)
     */
    protected HeaderRequestMatcher(final Matcher<? super List<String>> pred, final String headerName) {
        super(pred);

        Validate.notEmpty(headerName, "headerName cannot be empty");
        this.headerName = headerName;
        
        this.desc = "header \"" + headerName + "\" is";
    }


    /**
     * Retrieves a header (defined in {@link #HeaderRequestMatcher(org.hamcrest.Matcher, java.lang.String)})
     * of the given request.
     * @param req request to retrieve the header from
     * @return the request header as a list of values or {@code null} if there is no such a header in the request
     */
    @Override
    protected List<String> retrieveValue(final Request req) {
        return req.getHeaderValues(this.headerName);
    }
    

    /**
     * {@inheritDoc} 
     */
    @Override
    protected String provideDescription() {
        return this.desc;
    }


    /**
     * Factory method to create new instance of this matcher.
     * @param headerName name of a request header
     * @param pred a predicate to be applied on the request header
     * @return new instance of this matcher
     */
    @Factory
    public static HeaderRequestMatcher requestHeader(final String headerName,
            final Matcher<? super List<String>> pred) {
        return new HeaderRequestMatcher(pred, headerName);
    }    
}