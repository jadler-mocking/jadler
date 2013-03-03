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


public class HeaderRequestMatcher extends RequestMatcher<List<String>> {

    private final String headerName;
    private final String desc;


    public HeaderRequestMatcher(final Matcher<? super List<String>> pred, final String headerName) {
        super(pred);

        Validate.notEmpty(headerName, "headerName cannot be empty");
        this.headerName = headerName;
        
        this.desc = "header " + headerName + " is";
    }


    @Override
    protected List<String> retrieveValue(final Request req) throws Exception {
        List<String> headers = req.getHeaders(this.headerName);
        return ((headers != null) && !headers.isEmpty()) ? headers : null;
    }
    
    
    @Override
    protected String provideDescription() {
        return this.desc;
    }


    @Factory
    public static HeaderRequestMatcher requestHeader(final String headerName, final Matcher<? super List<String>> pred) {
        return new HeaderRequestMatcher(pred, headerName);
    }    
}