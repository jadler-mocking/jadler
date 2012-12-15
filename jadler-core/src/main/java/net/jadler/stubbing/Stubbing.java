package net.jadler.stubbing;

import net.jadler.rule.HttpMockResponse;
import net.jadler.rule.HttpMockRule;
import net.jadler.exception.JadlerException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matcher;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import net.jadler.Jadler;
import net.jadler.httpmocker.HttpMocker;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.Validate;

import static org.hamcrest.Matchers.*;
import static net.jadler.matchers.QueryStringRequestMatcher.requestQueryString;
import static net.jadler.matchers.MethodRequestMatcher.requestMethod;
import static net.jadler.matchers.BodyRequestMatcher.requestBody;
import static net.jadler.matchers.URIRequestMatcher.requestURI;
import static net.jadler.matchers.HeaderRequestMatcher.requestHeader;
import static net.jadler.matchers.ParameterRequestMatcher.requestParameter;



/**
 * Package private class for defining stubs in a fluid fashion. See {@link HttpMocker#onRequest()},
 * {@link Jadler#onRequest()} for more information on creating instances of this class.
 */
public class Stubbing implements RequestStubbing, ResponseStubbing {
    
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private final List<Matcher<? super HttpServletRequest>> matchers;
    private final List<HttpMockResponse> responses;
    private final MultiMap defaultHeaders;
    private final int defaultStatus;
    private final Charset defaultEncoding;
    
    
    /**
     * Package private constructor, you should never create new instance of this class on your own,
     * use {@link HttpMocker#onRequest()} instead.
     * @param defaultHeaders default headers to be added to every mock response
     * @param defaultStatus default http status of every mock response (can be overwritten for a particular response)
     * @param defaultEncoding default encoding of every stub response (can be overwritten in particular stub)
     */
    @SuppressWarnings("unchecked")
    Stubbing(final Charset defaultEncoding, final int defaultStatus, final MultiMap defaultHeaders) {
        
        this.matchers = new ArrayList<>();
        this.responses = new ArrayList<>();
        this.defaultHeaders = new MultiValueMap();
        this.defaultHeaders.putAll(defaultHeaders);
        this.defaultStatus = defaultStatus;
        this.defaultEncoding = defaultEncoding;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public RequestStubbing that(final Matcher<? super HttpServletRequest> matcher) {
        Validate.notNull(matcher, "matcher cannot be null");
        
        this.matchers.add(matcher);
        return this;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RequestStubbing havingMethodEqualTo(final String method) {
        return havingMethod(equalToIgnoringCase(method));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingMethod(final Matcher<? super String> pred) {
        return that(requestMethod(pred));
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingBodyEqualTo(final String requestBody) {
        return havingBody(equalTo(requestBody));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingBody(final Matcher<? super String> pred) {
        return that(requestBody(pred));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingURIEqualTo(final String uri) {
        Validate.isTrue(!uri.contains("?"), "URI must not contain query parameters.");
        return havingURI(equalTo(uri));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingURI(final Matcher<? super String> pred) {
        return that(requestURI(pred));
    }
    
    
    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingQueryStringEqualTo(final String queryString) {
        return havingQueryString(equalTo(queryString));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingQueryString(final Matcher<? super String> pred) {
        return that(requestQueryString(pred));
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingParameterEqualTo(final String name, final String value) {
        return havingParameter(name, hasItem(value));
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingParameter(final String name, final Matcher<? super List<String>> matcher) {
        return that(requestParameter(name, matcher));
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingParameter(final String name) {
        return havingParameter(name, notNullValue());
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingParameters(String... names) {
        
        for (final String name: names) {
            havingParameter(name);
        }
        
        return this;
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingHeaderEqualTo(final String name, final String value) {
        return havingHeader(name, hasItem(value));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingHeader(final String name, final Matcher<? super List<String>> pred) {
        return that(requestHeader(name, pred));
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingHeader(final String name) {
        return havingHeader(name, notNullValue());
    }
    

    /**
     * {@inheritDoc}
     */    
    @Override
    public RequestStubbing havingHeaders(String... names) {
        for (final String name: names) {
            havingHeader(name);
        }

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseStubbing respond() {
        return this.thenRespond();
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseStubbing thenRespond() {
        final HttpMockResponse response = new HttpMockResponse();
        
        response.addHeaders(defaultHeaders);
        response.setStatus(defaultStatus);
        response.setEncoding(defaultEncoding);
        response.setBody("");
        
        responses.add(response);
        return this;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseStubbing withContentType(final String contentType) {
        currentResponse().setHeaderCaseInsensitive(CONTENT_TYPE_HEADER, contentType);
        return this;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseStubbing withEncoding(final Charset encoding) {
        currentResponse().setEncoding(encoding);
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseStubbing withBody(final String responseBody) {
        currentResponse().setBody(responseBody);
        return this;
    }


    /**
     * {@inheritDoc}
     */    
    @Override
    public ResponseStubbing withBody(final Reader reader) {
        try {
            final String responseBody;
            
            try {
                responseBody = IOUtils.toString(reader);
            } catch (IOException ex) {
                throw new JadlerException("An error ocurred while reading the response body from "
                        + "the given Reader instance.", ex);
            }
            
            return this.withBody(responseBody);
        }
        finally {
            IOUtils.closeQuietly(reader);
        }
    }

    
    /**
     * {@inheritDoc}
     */ 
    @Override
    public ResponseStubbing withHeader(final String name, final String value) {
        currentResponse().addHeader(name, value);
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseStubbing withStatus(final int status) {
        currentResponse().setStatus(status);
        return this;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseStubbing withTimeout(long timeout, TimeUnit timeUnit) {
        currentResponse().setTimeout(java.util.concurrent.TimeUnit.MILLISECONDS.convert(timeout, timeUnit));
        return this;
    }
    
    
    /**
     * Creates HttpMockRule instance from this Stubbing instance.
     * @return HttpMockRule instance configured using values from this Stubbing
     */
    public HttpMockRule createRule() {
        return new HttpMockRule(matchers, responses);
    }

    
    /**
     * package private getter for testing purposes
     * @return all registered matchers
     */
    List<Matcher<? super HttpServletRequest>> getMatchers() {
        return new ArrayList<>(this.matchers);
    }
    
    
    /**
     * package private getter for testing purposes
     * @return all defined responses
     */
    List<HttpMockResponse> getResponses() {
        return new ArrayList<>(this.responses);
    }
    

    private HttpMockResponse currentResponse() {
        return responses.get(responses.size() - 1);
    }
}