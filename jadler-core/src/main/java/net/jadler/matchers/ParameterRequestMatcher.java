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
 * A {@link RequestMatcher} used for matching a request parameter.
 */
public class ParameterRequestMatcher extends RequestMatcher<List<String>> {

    private final String paramName;
    private final String desc;


    /**
     * Protected constructor useful only when subtyping. For creating instances of this class use 
     * {@link #requestParameter(java.lang.String, org.hamcrest.Matcher)} instead.
     * @param pred a predicate to be applied on the given request parameter
     * @param paramName name of a request parameter (case sensitive)
     */
    public ParameterRequestMatcher(final Matcher<? super List<String>> pred, final String paramName) {
        super(pred);

        Validate.notEmpty(paramName, "paramName cannot be empty");
        this.paramName = paramName;
        
        this.desc = "parameter \"" + paramName + "\" is";
    }


    /**
     * Retrieves a parameter (defined in {@link #ParameterRequestMatcher(org.hamcrest.Matcher, java.lang.String)})
     * of the given request. The values are percent-encoded.
     * @param req request to retrieve the parameter from
     * @return the request parameter as a list of values or {@code null} if there is no such a parameter in the request
     */
    @Override
    protected List<String> retrieveValue(final Request req) {
        return req.getParameters().getValues(this.paramName);
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
     * @param paramName name of a request parameter
     * @param pred a predicate to be applied on the request parameter
     * @return new instance of this matcher
     */
    @Factory
    public static ParameterRequestMatcher requestParameter(final String paramName, 
            final Matcher<? super List<String>> pred) {
        return new ParameterRequestMatcher(pred, paramName);
    }
}