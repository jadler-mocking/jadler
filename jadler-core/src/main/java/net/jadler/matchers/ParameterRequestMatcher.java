/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.matchers;

import java.util.List;

import net.jadler.stubbing.Request;
import org.apache.commons.lang.Validate;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;


public class ParameterRequestMatcher extends RequestMatcher<List<String>> {

    private final String paramName;
    private final String desc;


    public ParameterRequestMatcher(final Matcher<? super List<String>> pred, final String paramName) {
        super(pred);

        Validate.notEmpty(paramName, "paramName cannot be empty");
        this.paramName = paramName;
        
        this.desc = "parameter " + paramName + " is";
    }


    @Override
    protected List<String> retrieveValue(final Request req) throws Exception {
        return req.getParameters(this.paramName);
    }
    
    
    @Override
    protected String provideDescription() {
        return this.desc;
    }


    @Factory
    public static ParameterRequestMatcher requestParameter(final String paramName, final Matcher<? super List<String>> pred) {
        return new ParameterRequestMatcher(pred, paramName);
    }
}