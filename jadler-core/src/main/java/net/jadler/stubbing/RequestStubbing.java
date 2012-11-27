package net.jadler.stubbing;

import org.hamcrest.Matcher;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * This interface defines methods for the <i>WHEN</i> part of this http stubbing. These methods provides
 * a way to define rules the incoming http request is matched against. If the request matches, a response defined
 * by this stubbing is returned.
 */
public interface RequestStubbing {

    /**
     * Adds a general hamcrest matcher to define the <i>WHEN</i> part of this http stubbing.
     * @param matcher to be added to the <i>WHEN</i> part of this http stubbing
     * @return this stubbing
     */
    RequestStubbing that(Matcher<? super HttpServletRequest> matcher);


    /**
     * The request method must be equal (ignore case) to the given value in order to apply this mock rule.
     * @param method expected http method of the incoming http request
     * @return this stubbing 
     */
    RequestStubbing havingMethodEqualTo(String method);


    /**
     * The request method must match the given matcher in order to apply this mock rule.
     * @param pred matcher to be applied on the request method value
     * @return this stubbing
     */
    RequestStubbing havingMethod(Matcher<? super String> pred);


    /**
     * The request body must be equal to the given value in order to apply this mock rule.
     * @param requestBody expected body of the incoming http request
     * @return this stubbing 
     */
    RequestStubbing havingBodyEqualTo(String requestBody);

    
    /**
     * The request body must match the given matcher in order to apply this mock rule.
     * @param pred matcher to be applied on the request body
     * @return this stubbing
     */
    RequestStubbing havingBody(Matcher<? super String> pred);


    /**
     * The request URI (as retrieved by {@link HttpServletRequest#getRequestURI()})
     * must match the given matcher in order to apply this mock rule.
     * @param uri expected URI of the incoming http request
     * @return this stubbing
     */
    RequestStubbing havingURIEqualTo(String uri);


    /**
     * The request URI (as retrieved by {@link HttpServletRequest#getRequestURI()})
     * must match the given matcher in order to apply this mock rule.
     * @param pred matcher to be applied on the request URI
     * @return this stubbing
     */
    RequestStubbing havingURI(Matcher<? super String> pred);
    
    
    /**
     * The query string (as retrieved by {@link HttpServletRequest#getQueryString()})
     * must be equal to the given value in order to apply this mock rule.
     * @param queryString expected query string of the incoming http request
     * @return this stubbing 
     */
    RequestStubbing havingQueryStringEqualTo(String queryString);


    /**
     * The query string (as retrieved by {@link HttpServletRequest#getQueryString()})
     * must match the given matcher in order to apply this mock rule.
     * @param pred matcher to be applied on the query string
     * @return this stubbing
     */
    RequestStubbing havingQueryString(Matcher<? super String> pred);
    

    /**
     * The given http parameter must be present in the request body and one of its values
     * must be equal to the given value in order to apply this mock rule.
     * @param name parameter name
     * @param value expected parameter value
     * @return this stubbing 
     */
    RequestStubbing havingParameterEqualTo(String name, String value);

    
    /**
     * The parameter with the given name must match the given matcher in order to apply this mock rule.
     * @param name parameter name
     * @param matcher matcher to be applied on the parameter values
     * @return this stubbing
     */
    RequestStubbing havingParameter(String name, Matcher<? super List<String>> matcher);


    /**
     * The parameter with the given name must exist in order to apply this mock rule.
     * @param name parameter name
     * @return this stubbing
     */
    RequestStubbing havingParameter(String name);


    /**
     * The given parameters must exist in order to apply this mock rule.
     * @param names parameter names
     * @return this stubbing
     */
    RequestStubbing havingParameters(String... names);


    /**
     * The given request header must be present in the request body and one of its values
     * must be equal to the given value in order to apply this mock rule.
     * @param name header name
     * @param value expected header value
     * @return this stubbing 
     */
    RequestStubbing havingHeaderEqualTo(String name, String value);


    /**
     * The given request header must match the given matcher in order to apply this mock rule.
     * @param name parameter name
     * @param matcher matcher to be applied on the header values
     * @return this stubbing
     */
    RequestStubbing havingHeader(String name, Matcher<? super List<String>> matcher);


    /**
     * The request header with the given name must exist in order to apply this mock rule.
     * @param name header name
     * @return this stubbing
     */
    RequestStubbing havingHeader(final String name);


    /**
     * The given headers must exist in order to apply this mock rule.
     * @param names header names
     * @return this stubbing
     */
    RequestStubbing havingHeaders(String... names);


    /**
     * Finish the <i>WHEN</i> part of the stubbing and start the <i>THEN</i> part.
     * @return response stubbing instance to continue the stubbing
     */
    ResponseStubbing respond();
}
