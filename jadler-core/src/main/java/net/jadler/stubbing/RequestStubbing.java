/*
 * Copyright (c) 2013 Jadler contributors
 * This program is made available under the terms of the MIT License.
 */
package net.jadler.stubbing;

import org.hamcrest.Matcher;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * This interface defines methods for the http stubbing <i>WHEN</i> part. These methods provides
 * a way to define predicates (in form of Hamcrest matchers) the incoming http request must fulfill in order to
 * return a stub response (defined by methods of {@link ResponseStubbing}).
 */
public interface RequestStubbing {

    /**
     * Adds a request predicate to define the <i>WHEN</i> part of this stubbing.
     * @param predicate request predicate to be added to the <i>WHEN</i> part of this stubbing
     * @return this ongoing stubbing
     */
    RequestStubbing that(Matcher<? super Request> predicate);


    /**
     * Adds a request method predicate. The request method must be equal (case insensitive) to the given value. 
     * @param method expected http method of the incoming http request
     * @return this ongoing stubbing 
     */
    RequestStubbing havingMethodEqualTo(String method);


    /**
     * Adds a request method predicate.
     * @param predicate request method predicate
     * @return this ongoing stubbing
     */
    RequestStubbing havingMethod(Matcher<? super String> predicate);


    /**
     * Adds a request body predicate. The request body must be equal to the given value. 
     * @param requestBody expected body of the incoming http request
     * @return this ongoing stubbing 
     */
    RequestStubbing havingBodyEqualTo(String requestBody);

    
    /**
     * Adds a request body predicate.
     * @param predicate request body predicate
     * @return this ongoing stubbing
     */
    RequestStubbing havingBody(Matcher<? super String> predicate);
    
    
    /**
     * Adds a request body predicate. The request body must be equal to the given value.
     * @param requestBody expected body of the incoming http request
     * @return this ongoing stubbing
     */
    RequestStubbing havingRawBodyEqualTo(byte[] requestBody);


    /**
     * Adds a request URI (as retrieved by {@link HttpServletRequest#getRequestURI()}) predicate.
     * The request URI must be equal to the given value. 
     * @param uri expected URI of the incoming http request
     * @return this ongoing stubbing
     */
    RequestStubbing havingURIEqualTo(String uri);


    /**
     * Adds a request URI (as retrieved by {@link HttpServletRequest#getRequestURI()}) predicate.
     * @param predicate request URI predicate
     * @return this ongoing stubbing
     */
    RequestStubbing havingURI(Matcher<? super String> predicate);
    
    
    /**
     * Adds a query string (as retrieved by {@link HttpServletRequest#getQueryString()}) predicate.
     * The query string value must be equal to the given value. 
     * @param queryString expected query string of the incoming http request
     * @return this ongoing stubbing 
     */
    RequestStubbing havingQueryStringEqualTo(String queryString);


    /**
     * Adds a query string (as retrieved by {@link HttpServletRequest#getQueryString()}) predicate.
     * @param predicate query string predicate
     * @return this ongoing stubbing
     */
    RequestStubbing havingQueryString(Matcher<? super String> predicate);
    

    /**
     * Adds a request parameter predicate. The given http parameter must be present
     * in the request body and at least one of its values must be equal to the given value.
     * @param name parameter name
     * @param value expected parameter value
     * @return this ongoing stubbing 
     */
    RequestStubbing havingParameterEqualTo(String name, String value);

    
    /**
     * Adds a request parameter predicate. 
     * @param name parameter name
     * @param predicate parameter predicate
     * @return this ongoing stubbing
     */
    RequestStubbing havingParameter(String name, Matcher<? super List<String>> predicate);


    /**
     * Adds a request parameter existence predicate. The given http parameter must be present
     * in the request body
     * @param name parameter name
     * @return this ongoing stubbing
     */
    RequestStubbing havingParameter(String name);


    /**
     * Adds a request parameters existence predicate. All of the given http parameters must be present
     * in the request body.
     * @param names parameter names
     * @return this ongoing stubbing
     */
    RequestStubbing havingParameters(String... names);


    /**
     * Adds a request header predicate. The given http header must be present
     * in the request body and at least one of its values must be equal to the given value.
     * @param name header name
     * @param value expected header value
     * @return this ongoing stubbing 
     */
    RequestStubbing havingHeaderEqualTo(String name, String value);


    /**
     * Adds a request header predicate. 
     * @param name header name
     * @param predicate header predicate
     * @return this ongoing stubbing
     */
    RequestStubbing havingHeader(String name, Matcher<? super List<String>> predicate);


    /**
     * Adds a request header existence predicate. The given http header must be present
     * in the request body
     * @param name header name
     * @return this ongoing stubbing
     */
    RequestStubbing havingHeader(final String name);


    /**
     * Adds a request headers existence predicate. All of the given http headers must be present
     * in the request body.
     * @param names headers names
     * @return this ongoing stubbing
     */
    RequestStubbing havingHeaders(String... names);


    /**
     * Finishes the <i>WHEN</i> part of this stubbing and starts the <i>THEN</i> part.
     * @return response stubbing instance to continue this stubbing
     */
    ResponseStubbing respond();
}
